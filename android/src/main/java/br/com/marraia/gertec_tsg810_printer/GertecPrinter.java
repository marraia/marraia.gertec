package br.com.marraia.gertec_tsg810_printer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.util.Log;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import br.com.gertec.gedi.GEDI;
import br.com.gertec.gedi.enums.GEDI_PRNTR_e_Alignment;
import br.com.gertec.gedi.enums.GEDI_PRNTR_e_BarCodeType;
import br.com.gertec.gedi.enums.GEDI_PRNTR_e_Status;
import br.com.gertec.gedi.exceptions.GediException;
import br.com.gertec.gedi.interfaces.IGEDI;
import br.com.gertec.gedi.interfaces.IPRNTR;
import br.com.gertec.gedi.structs.GEDI_PRNTR_st_BarCodeConfig;
import br.com.gertec.gedi.structs.GEDI_PRNTR_st_PictureConfig;
import br.com.gertec.gedi.structs.GEDI_PRNTR_st_StringConfig;

public class GertecPrinter {

    private static final String TAG = "gertec_tsg810";

    private final Activity activity;
    private final IPRNTR prntr;

    public GertecPrinter(Activity activity) {
        this.activity = activity;
        IGEDI gedi = GEDI.getInstance(activity);
        this.prntr = gedi.getPRNTR();
        try {
            this.prntr.Init();
        } catch (GediException e) {
            Log.e(TAG, "printer Init error", e);
        }
    }

    // region ─── Text ──────────────────────────────────────────────────────────

    public int imprimirTexto(Map map) {
        String text = (String) map.get("text");
        int alignValue = (Integer) map.get("align");
        boolean bold = Boolean.TRUE.equals(map.get("bold"));
        boolean italic = Boolean.TRUE.equals(map.get("italic"));
        boolean underline = Boolean.TRUE.equals(map.get("underline"));
        int fontSize = (Integer) map.get("fontSize");
        int lineSpacing = (Integer) map.get("lineSpacing");

        try {
            bufferText(toAlignment(alignValue), bold, italic, underline, fontSize, lineSpacing, text);
            prntr.Output();
            return 0;
        } catch (GediException e) {
            Log.e(TAG, "imprimirTexto error", e);
            return -1;
        }
    }

    // endregion

    // region ─── Feed ──────────────────────────────────────────────────────────

    public int avancarPapel(int lines) {
        try {
            prntr.DrawBlankLine(lines);
            prntr.Output();
            return 0;
        } catch (GediException e) {
            Log.e(TAG, "avancarPapel error", e);
            return -1;
        }
    }

    // endregion

    // region ─── Barcode ───────────────────────────────────────────────────────

    public int imprimirCodigoBarras(Map map) {
        String data = (String) map.get("data");
        String typeStr = (String) map.get("type");
        String sizeStr = (String) map.get("size");

        try {
            GEDI_PRNTR_st_BarCodeConfig config = new GEDI_PRNTR_st_BarCodeConfig(
                    toBarcodeType(typeStr), toBarcodeHeight(sizeStr), toBarcodeWidth(sizeStr));
            prntr.DrawBarCode(config, data);
            prntr.Output();
            return 0;
        } catch (GediException e) {
            Log.e(TAG, "imprimirCodigoBarras error", e);
            return -1;
        }
    }

    // endregion

    // region ─── Image ─────────────────────────────────────────────────────────

    public int imprimirImagemBytes(Map map) {
        byte[] imageBytes = (byte[]) map.get("imageBytes");
        int width = (Integer) map.get("width");
        int height = (Integer) map.get("height");
        int alignValue = (Integer) map.get("align");

        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        if (bitmap == null) {
            Log.e(TAG, "imprimirImagemBytes: bitmap decode failed");
            return -1;
        }

        try {
            GEDI_PRNTR_st_PictureConfig config = new GEDI_PRNTR_st_PictureConfig(
                    toAlignment(alignValue), 0, width > 0 ? width : 0, height > 0 ? height : 0);
            prntr.DrawPictureExt(config, bitmap);
            prntr.Output();
            return 0;
        } catch (GediException e) {
            Log.e(TAG, "imprimirImagemBytes error", e);
            return -1;
        }
    }

    public int imprimirImagemUrl(String imageUrl) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            Log.d(TAG, "imprimirImagemUrl url=" + imageUrl);
            URL url = new URL(imageUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setDoInput(true);
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "imprimirImagemUrl responseCode=" + responseCode);
                return -1;
            }
            inputStream = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap == null) {
                Log.e(TAG, "imprimirImagemUrl bitmap decode failed");
                return -1;
            }
            GEDI_PRNTR_st_PictureConfig config = new GEDI_PRNTR_st_PictureConfig(GEDI_PRNTR_e_Alignment.CENTER);
            prntr.DrawPictureExt(config, bitmap);
            prntr.Output();
            return 0;
        } catch (Exception e) {
            Log.e(TAG, "imprimirImagemUrl failed", e);
            return -1;
        } finally {
            try { if (inputStream != null) inputStream.close(); } catch (Exception ignored) {}
            if (connection != null) connection.disconnect();
        }
    }

    // endregion

    // region ─── HTML (not supported in GEDI) ──────────────────────────────────

    public int imprimirHtml(String html) {
        Log.w(TAG, "imprimirHtml: not supported in GEDI");
        return -1;
    }

    // endregion

    // region ─── Status ────────────────────────────────────────────────────────

    public int obterStatus() {
        try {
            GEDI_PRNTR_e_Status status = prntr.Status();
            return status.getValue();
        } catch (GediException e) {
            Log.e(TAG, "obterStatus error", e);
            return -1;
        }
    }

    public int obterUsoPapel() {
        try {
            return prntr.GetPaperUsage();
        } catch (GediException e) {
            Log.e(TAG, "obterUsoPapel error", e);
            return -1;
        }
    }

    public int resetarUsoPapel() {
        try {
            prntr.ResetPaperUsage();
            return 0;
        } catch (GediException e) {
            Log.e(TAG, "resetarUsoPapel error", e);
            return -1;
        }
    }

    // endregion

    // region ─── Order Printing ────────────────────────────────────────────────

    public int imprimirPedido(Map params) {
        String dados = (String) params.get("dados");
        int posicao = (Integer) params.get("posicao");
        int stilo = (Integer) params.get("stilo");
        int tamanho = (Integer) params.get("tamanho");
        String data = (String) params.get("data");
        String senha = (String) params.get("senha");
        String imageUrl = (String) params.get("imageUrl");

        Log.d(TAG, "imprimirPedido dados=" + dados);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            int result = imprimirImagemUrl(imageUrl);
            Log.d(TAG, "imprimirPedido imageResult=" + result);
            if (result != 0) return result;
        }

        GEDI_PRNTR_e_Alignment align = toAlignment(posicao);
        boolean bold = stilo == 8;
        int fontSize = tamanho;

        try {
            bufferSeparador();
            prntr.DrawBlankLine(ESPACO);
            bufferQuebrado(align, bold, fontSize, dados);
            prntr.DrawBlankLine(ESPACO);

            if (senha != null && !senha.isEmpty()) {
                bufferSeparador();
                bufferQuebrado(align, true, FONTE_DESTAQUE, senha);
            }

            bufferSeparador();
            bufferText(GEDI_PRNTR_e_Alignment.CENTER, false, false, false, FONTE_NORMAL, 0, data);
            prntr.DrawBlankLine(ESPACO);
            bufferRodape();
            prntr.Output();
            return 0;
        } catch (GediException e) {
            Log.e(TAG, "imprimirPedido error", e);
            return -1;
        }
    }

    public int imprimirPedidoConsolidado(Map params) {
        List<Map> itemsForCart = (List<Map>) params.get("items");
        String orderNumber = (String) params.get("orderNumber");
        boolean takeHome = Boolean.TRUE.equals(params.get("takeHome"));
        String imageUrl = (String) params.get("imageUrl");

        Log.d(TAG, "imprimirPedidoConsolidado orderNumber=" + orderNumber + " takeHome=" + takeHome);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            int result = imprimirImagemUrl(imageUrl);
            Log.d(TAG, "imprimirPedidoConsolidado imageResult=" + result);
            if (result != 0) return result;
        }

        try {
            bufferSeparador();
            prntr.DrawBlankLine(ESPACO);
            bufferText(GEDI_PRNTR_e_Alignment.CENTER, true, false, false, FONTE_TITULO, 0, "PEDIDO #" + safeString(orderNumber));

            String now = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR")).format(new Date());
            bufferText(GEDI_PRNTR_e_Alignment.CENTER, false, false, false, FONTE_NORMAL, 0, now);
            prntr.DrawBlankLine(ESPACO);

            double total = 0.0;
            if (itemsForCart != null) {
                for (int i = 0; i < itemsForCart.size(); i++) {
                    Map item = itemsForCart.get(i);
                    String description = removerAcentos(safeString(item.get("description")).toUpperCase());
                    Number valueNumber = (Number) item.get("value");
                    double value = valueNumber != null ? valueNumber.doubleValue() : 0.0;
                    total += value;
                    // Nome à esquerda e valor no fim da linha; observações logo abaixo
                    bufferEsquerdaDireita(true, FONTE_ITEM, (i + 1) + ". " + description,
                            String.format(new Locale("pt", "BR"), "R$ %.2f", value));

                    String category = safeString(item.get("category")).toLowerCase();
                    boolean isBeverage = category.contains("bebida");
                    List itemsToShow = (List) (isBeverage
                            ? item.get("selectedIngredients")
                            : item.get("removedIngredients"));
                    if (itemsToShow != null && !itemsToShow.isEmpty()) {
                        String label = isBeverage ? "Escolha:" : "Remover:";
                        bufferQuebrado(GEDI_PRNTR_e_Alignment.LEFT, true, FONTE_NORMAL,
                                "   " + label + " " + juntarItens(itemsToShow));
                    }
                    prntr.DrawBlankLine(ESPACO);
                }
            }

            prntr.DrawBlankLine(ESPACO);
            bufferEsquerdaDireita(true, FONTE_DESTAQUE, "TOTAL",
                    String.format(new Locale("pt", "BR"), "R$ %.2f", total));
            prntr.DrawBlankLine(ESPACO);
            bufferSeparador();
            prntr.DrawBlankLine(ESPACO);
            bufferQuebrado(GEDI_PRNTR_e_Alignment.CENTER, true, FONTE_ITEM,
                    removerAcentos(takeHome ? "PARA VIAGEM" : "PARA CONSUMIR NO LOCAL"));
            prntr.DrawBlankLine(ESPACO);
            bufferSeparador();
            prntr.DrawBlankLine(ESPACO * 2);
            bufferRodape();
            prntr.Output();
            return 0;
        } catch (GediException e) {
            Log.e(TAG, "imprimirPedidoConsolidado error", e);
            return -1;
        }
    }

    // endregion

    // region ─── Private Helpers ───────────────────────────────────────────────

    // Papel de 58mm na GPOS800/G800: 384 pontos de largura. Tamanhos em pontos (Paint.setTextSize).
    private static final int LARGURA_PAPEL = 384;
    private static final int FONTE_TITULO = 40;
    private static final int FONTE_DESTAQUE = 32;
    private static final int FONTE_ITEM = 28;
    private static final int FONTE_NORMAL = 24;
    private static final int FONTE_RODAPE = 20;
    // DrawBlankLine(n) desenha n PIXELS em branco (8 px ≈ 1 mm)
    private static final int ESPACO = 12;
    private static final int AVANCO_CORTE = 160; // ≈ 20 mm: da cabeça de impressão até a serrilha

    /**
     * Texto à esquerda e valor encostado à direita na mesma linha (preenche com espaços).
     * Se o texto for longo, quebra por palavra e o valor fica na última linha (ou numa linha própria).
     */
    private void bufferEsquerdaDireita(boolean bold, int fontSize, String left, String right) throws GediException {
        Paint paint = new Paint();
        paint.setTextSize(fontSize);
        paint.setFakeBoldText(bold);
        float espacoValor = paint.measureText(" " + right);

        // quebra o texto da esquerda deixando lugar para o valor na última linha
        java.util.List<String> linhas = new java.util.ArrayList<>();
        StringBuilder linha = new StringBuilder();
        for (String palavra : safeString(left).trim().split("\\s+")) {
            String tentativa = linha.length() == 0 ? palavra : linha + " " + palavra;
            if (linha.length() > 0 && paint.measureText(tentativa) > LARGURA_PAPEL) {
                linhas.add(linha.toString());
                linha = new StringBuilder(palavra);
            } else {
                linha = new StringBuilder(tentativa);
            }
        }
        linhas.add(linha.toString());

        String ultima = linhas.remove(linhas.size() - 1);
        for (String l : linhas) bufferText(GEDI_PRNTR_e_Alignment.LEFT, bold, false, false, fontSize, 0, l);

        if (paint.measureText(ultima) + espacoValor > LARGURA_PAPEL) {
            bufferText(GEDI_PRNTR_e_Alignment.LEFT, bold, false, false, fontSize, 0, ultima);
            bufferText(GEDI_PRNTR_e_Alignment.RIGHT, bold, false, false, fontSize, 0, right);
            return;
        }
        StringBuilder junta = new StringBuilder(ultima).append(" ");
        while (paint.measureText(junta + " " + right) <= LARGURA_PAPEL) junta.append(" ");
        bufferText(GEDI_PRNTR_e_Alignment.LEFT, bold, false, false, fontSize, 0, junta + right);
    }

    /** Linha de "=" ocupando a largura do papel */
    private void bufferSeparador() throws GediException {
        Paint paint = new Paint();
        paint.setTextSize(FONTE_NORMAL);
        int n = (int) (LARGURA_PAPEL / paint.measureText("=")) - 1;
        StringBuilder linha = new StringBuilder();
        for (int i = 0; i < n; i++) linha.append("=");
        bufferText(GEDI_PRNTR_e_Alignment.CENTER, false, false, false, FONTE_NORMAL, 0, linha.toString());
    }

    /** Texto com quebra de linha por palavra, para não cortar no fim do papel com fonte grande */
    private void bufferQuebrado(GEDI_PRNTR_e_Alignment align, boolean bold, int fontSize, String text) throws GediException {
        Paint paint = new Paint();
        paint.setTextSize(fontSize);
        paint.setFakeBoldText(bold);
        StringBuilder linha = new StringBuilder();
        for (String palavra : safeString(text).split(" ")) {
            String tentativa = linha.length() == 0 ? palavra : linha + " " + palavra;
            if (linha.length() > 0 && paint.measureText(tentativa) > LARGURA_PAPEL) {
                bufferText(align, bold, false, false, fontSize, 0, linha.toString());
                linha = new StringBuilder(palavra);
            } else {
                linha = new StringBuilder(tentativa);
            }
        }
        if (linha.length() > 0) bufferText(align, bold, false, false, fontSize, 0, linha.toString());
    }

    private void bufferText(GEDI_PRNTR_e_Alignment align, boolean bold, boolean italic,
                            boolean underline, int fontSize, int lineSpace, String text) throws GediException {
        Paint paint = new Paint();
        paint.setTextSize(fontSize);
        paint.setFakeBoldText(bold);
        paint.setUnderlineText(underline);
        if (italic) paint.setTextSkewX(-0.25f);
        switch (align) {
            case CENTER: paint.setTextAlign(Paint.Align.CENTER); break;
            case RIGHT:  paint.setTextAlign(Paint.Align.RIGHT);  break;
            default:     paint.setTextAlign(Paint.Align.LEFT);   break;
        }
        prntr.DrawStringExt(new GEDI_PRNTR_st_StringConfig(paint, lineSpace, 0), text);
    }

    private void bufferRodape() throws GediException {
        String version = obterVersaoApp();
        bufferText(GEDI_PRNTR_e_Alignment.CENTER, true, false, false, FONTE_RODAPE, 0, "@eventflow v" + version);
        bufferText(GEDI_PRNTR_e_Alignment.CENTER, true, false, false, FONTE_RODAPE, 0, "powered by @marraia");
        // Papel em branco até a serrilha: o cupom sai inteiro no corte
        prntr.DrawBlankLine(AVANCO_CORTE);
    }

    private GEDI_PRNTR_e_Alignment toAlignment(int value) {
        switch (value) {
            case 1: return GEDI_PRNTR_e_Alignment.CENTER;
            case 2: return GEDI_PRNTR_e_Alignment.RIGHT;
            default: return GEDI_PRNTR_e_Alignment.LEFT;
        }
    }

    private int tamanhoToFontSize(int tamanho) {
        if (tamanho >= 24) return 28;
        if (tamanho >= 16) return 20;
        return 14;
    }

    private GEDI_PRNTR_e_BarCodeType toBarcodeType(String typeStr) {
        if (typeStr == null) return GEDI_PRNTR_e_BarCodeType.CODE_128;
        switch (typeStr) {
            case "AZTEC":       return GEDI_PRNTR_e_BarCodeType.AZTEC;
            case "CODABAR":     return GEDI_PRNTR_e_BarCodeType.CODABAR;
            case "CODE_39":     return GEDI_PRNTR_e_BarCodeType.CODE_39;
            case "CODE_93":     return GEDI_PRNTR_e_BarCodeType.CODE_93;
            case "DATA_MATRIX": return GEDI_PRNTR_e_BarCodeType.DATA_MATRIX;
            case "EAN_8":       return GEDI_PRNTR_e_BarCodeType.EAN_8;
            case "EAN_13":      return GEDI_PRNTR_e_BarCodeType.EAN_13;
            case "ITF":         return GEDI_PRNTR_e_BarCodeType.ITF;
            case "PDF_417":     return GEDI_PRNTR_e_BarCodeType.PDF_417;
            case "QR_CODE":     return GEDI_PRNTR_e_BarCodeType.QR_CODE;
            case "UPC_A":       return GEDI_PRNTR_e_BarCodeType.UPC_A;
            case "UPC_E":       return GEDI_PRNTR_e_BarCodeType.UPC_E;
            default:            return GEDI_PRNTR_e_BarCodeType.CODE_128;
        }
    }

    private int toBarcodeHeight(String sizeStr) {
        if ("SMALL".equals(sizeStr)) return 60;
        if ("HALF_PAPER".equals(sizeStr)) return 100;
        return 150;
    }

    private int toBarcodeWidth(String sizeStr) {
        if ("SMALL".equals(sizeStr)) return 200;
        if ("HALF_PAPER".equals(sizeStr)) return 300;
        return 0;
    }

    private String removerAcentos(String value) {
        if (value == null) return "";
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private String safeString(Object value) {
        return value == null ? "" : value.toString();
    }

    private String juntarItens(List items) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) builder.append(", ");
            builder.append(removerAcentos(safeString(items.get(i))));
        }
        return builder.toString();
    }

    private String obterVersaoApp() {
        try {
            return activity.getPackageManager()
                    .getPackageInfo(activity.getPackageName(), 0)
                    .versionName;
        } catch (Exception e) {
            Log.e(TAG, "obterVersaoApp failed", e);
            return "1.0";
        }
    }

    // endregion
}
