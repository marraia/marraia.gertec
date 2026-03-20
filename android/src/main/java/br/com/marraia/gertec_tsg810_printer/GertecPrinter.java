package br.com.marraia.gertec_tsg810_printer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import br.com.gertec.easylayer.printer.Alignment;
import br.com.gertec.easylayer.printer.BarcodeFormat;
import br.com.gertec.easylayer.printer.BarcodeType;
import br.com.gertec.easylayer.printer.PrintConfig;
import br.com.gertec.easylayer.printer.Printer;
import br.com.gertec.easylayer.printer.PrinterError;
import br.com.gertec.easylayer.printer.PrinterException;
import br.com.gertec.easylayer.printer.PrinterUtils;
import br.com.gertec.easylayer.printer.Status;
import br.com.gertec.easylayer.printer.TextFormat;

public class GertecPrinter implements Printer.Listener {

    private static final String TAG = "gertec_tsg810";

    private final Activity activity;
    private final Printer printer;
    private final PrinterUtils printerUtils;

    public GertecPrinter(Activity activity) {
        this.activity = activity;
        this.printer = Printer.getInstance(activity, this);
        this.printerUtils = printer.getPrinterUtils();
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

        TextFormat tf = new TextFormat();
        tf.setAlignment(toAlignment(alignValue));
        tf.setBold(bold);
        tf.setItalic(italic);
        tf.setUnderscore(underline);
        tf.setFontSize(fontSize);
        if (lineSpacing > 0) {
            tf.setLineSpacing(lineSpacing);
        }

        return printer.printText(tf, text);
    }

    // endregion

    // region ─── Feed ──────────────────────────────────────────────────────────

    public int avancarPapel(int lines) {
        return printer.scrollPaper(lines);
    }

    // endregion

    // region ─── Barcode ───────────────────────────────────────────────────────

    public int imprimirCodigoBarras(Map map) {
        String data = (String) map.get("data");
        String typeStr = (String) map.get("type");
        String sizeStr = (String) map.get("size");

        BarcodeType barcodeType = toBarcodeType(typeStr);
        BarcodeFormat.Size barcodeSize = toBarcodeSize(sizeStr);

        BarcodeFormat format = new BarcodeFormat(barcodeType);
        format.setSize(barcodeSize);

        return printer.printBarcode(format, data);
    }

    // endregion

    // region ─── Image ─────────────────────────────────────────────────────────

    public int imprimirImagemBytes(Map map) {
        byte[] imageBytes = (byte[]) map.get("imageBytes");
        int width = (Integer) map.get("width");
        int height = (Integer) map.get("height");
        int alignValue = (Integer) map.get("align");
        boolean autoResize = Boolean.TRUE.equals(map.get("autoResize"));

        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        if (bitmap == null) {
            Log.e(TAG, "imprimirImagemBytes: bitmap decode failed");
            return -1;
        }

        try {
            if (autoResize) {
                return printer.printImageAutoResize(bitmap);
            } else {
                PrintConfig config = new PrintConfig();
                if (width > 0) config.setWidth(width);
                if (height > 0) config.setHeight(height);
                config.setAlignment(toAlignment(alignValue));
                return printer.printImage(config, bitmap);
            }
        } catch (PrinterException e) {
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
            return printer.printImageAutoResize(bitmap);
        } catch (Exception e) {
            Log.e(TAG, "imprimirImagemUrl failed", e);
            return -1;
        } finally {
            try {
                if (inputStream != null) inputStream.close();
            } catch (Exception ignored) {}
            if (connection != null) connection.disconnect();
        }
    }

    // endregion

    // region ─── HTML ──────────────────────────────────────────────────────────

    public int imprimirHtml(String html) {
        try {
            return printer.printHtml(activity, html);
        } catch (PrinterException e) {
            Log.e(TAG, "imprimirHtml error", e);
            return -1;
        }
    }

    // endregion

    // region ─── Status ────────────────────────────────────────────────────────

    public int obterStatus() {
        try {
            Status status = printer.getStatus();
            return status.getCode();
        } catch (PrinterException e) {
            Log.e(TAG, "obterStatus error", e);
            return -1;
        }
    }

    public int obterUsoPapel() {
        try {
            return printer.getPaperUsage();
        } catch (PrinterException e) {
            Log.e(TAG, "obterUsoPapel error", e);
            return -1;
        }
    }

    public int resetarUsoPapel() {
        try {
            printer.resetPaperUsage();
            return 0;
        } catch (PrinterException e) {
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
            int imageResult = imprimirImagemUrl(imageUrl);
            Log.d(TAG, "imprimirPedido imageResult=" + imageResult);
            if (imageResult != 0) {
                return imageResult;
            }
        }

        Alignment alignment = toAlignment(posicao);
        boolean bold = stilo == 8;
        int fontSize = tamanhoToFontSize(tamanho);

        TextFormat separatorFmt = buildTextFormat(Alignment.CENTER, false, 14);
        TextFormat contentFmt = buildTextFormat(alignment, bold, fontSize);
        TextFormat boldSmallFmt = buildTextFormat(alignment, true, 14);
        TextFormat centerSmallFmt = buildTextFormat(Alignment.CENTER, false, 14);

        printer.printText(separatorFmt, "================================");
        printer.scrollPaper(1);
        printer.printText(contentFmt, dados);
        printer.scrollPaper(1);

        if (senha != null && !senha.isEmpty()) {
            printer.printText(separatorFmt, "================================");
            printer.printText(boldSmallFmt, senha);
        }

        printer.printText(separatorFmt, "================================");
        printer.printText(centerSmallFmt, data);
        printer.scrollPaper(1);

        return imprimirRodape();
    }

    public int imprimirPedidoConsolidado(Map params) {
        List<Map> itemsForCart = (List<Map>) params.get("items");
        String orderNumber = (String) params.get("orderNumber");
        boolean takeHome = Boolean.TRUE.equals(params.get("takeHome"));
        String imageUrl = (String) params.get("imageUrl");

        Log.d(TAG, "imprimirPedidoConsolidado orderNumber=" + orderNumber + " takeHome=" + takeHome);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            int imageResult = imprimirImagemUrl(imageUrl);
            Log.d(TAG, "imprimirPedidoConsolidado imageResult=" + imageResult);
            if (imageResult != 0) {
                return imageResult;
            }
        }

        TextFormat separatorFmt = buildTextFormat(Alignment.CENTER, true, 14);
        TextFormat headerFmt = buildTextFormat(Alignment.CENTER, true, 20);
        TextFormat dateFmt = buildTextFormat(Alignment.CENTER, false, 14);
        TextFormat itemTitleFmt = buildTextFormat(Alignment.LEFT, true, 14);
        TextFormat itemValueFmt = buildTextFormat(Alignment.LEFT, true, 14);
        TextFormat totalFmt = buildTextFormat(Alignment.LEFT, true, 20);
        TextFormat deliveryFmt = buildTextFormat(Alignment.CENTER, true, 14);

        printer.printText(separatorFmt, "================================");
        printer.scrollPaper(1);

        printer.printText(headerFmt, "PEDIDO #" + safeString(orderNumber));

        String now = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR")).format(new Date());
        printer.printText(dateFmt, now);
        printer.scrollPaper(1);

        double total = 0.0;
        if (itemsForCart != null) {
            for (int i = 0; i < itemsForCart.size(); i++) {
                Map item = itemsForCart.get(i);
                String description = removerAcentos(safeString(item.get("description")).toUpperCase());
                printer.printText(itemTitleFmt, (i + 1) + ". " + description);

                Number valueNumber = (Number) item.get("value");
                double value = valueNumber != null ? valueNumber.doubleValue() : 0.0;
                total += value;
                String valueText = String.format(Locale.US, "   R$ %.2f", value);
                printer.printText(itemValueFmt, valueText);

                String category = safeString(item.get("category")).toLowerCase();
                boolean isBeverage = category.contains("bebida");
                List itemsToShow = (List) (isBeverage ? item.get("selectedIngredients") : item.get("removedIngredients"));
                if (itemsToShow != null && !itemsToShow.isEmpty()) {
                    String ingredientLabel = isBeverage ? "Escolha:" : "Remover:";
                    String cleanedItems = juntarItens(itemsToShow);
                    printer.printText(itemValueFmt, "   " + ingredientLabel + " " + cleanedItems);
                }

                printer.scrollPaper(1);
            }
        }

        printer.scrollPaper(1);

        String totalText = String.format(Locale.US, "TOTAL: R$ %.2f", total);
        printer.printText(totalFmt, totalText);
        printer.scrollPaper(1);

        printer.printText(separatorFmt, "================================");
        printer.scrollPaper(1);

        String deliveryInfo = takeHome ? "PARA VIAGEM" : "PARA CONSUMIR NO LOCAL";
        printer.printText(deliveryFmt, removerAcentos(deliveryInfo));
        printer.scrollPaper(1);

        printer.printText(separatorFmt, "================================");
        printer.scrollPaper(2);

        return imprimirRodape();
    }

    // endregion

    // region ─── Private Helpers ───────────────────────────────────────────────

    private int imprimirRodape() {
        String version = obterVersaoApp();
        TextFormat footerFmt = buildTextFormat(Alignment.CENTER, true, 14);

        printer.printText(footerFmt, "@eventflow v" + version);
        printer.printText(footerFmt, "powered by @marraia");
        printer.scrollPaper(5);
        return 0;
    }

    private TextFormat buildTextFormat(Alignment alignment, boolean bold, int fontSize) {
        TextFormat tf = new TextFormat();
        tf.setAlignment(alignment);
        tf.setBold(bold);
        tf.setFontSize(fontSize);
        return tf;
    }

    private Alignment toAlignment(int value) {
        switch (value) {
            case 1: return Alignment.CENTER;
            case 2: return Alignment.RIGHT;
            default: return Alignment.LEFT;
        }
    }

    private int tamanhoToFontSize(int tamanho) {
        if (tamanho >= 24) return 28;
        if (tamanho >= 16) return 20;
        return 14;
    }

    private BarcodeType toBarcodeType(String typeStr) {
        if (typeStr == null) return BarcodeType.CODE_128;
        switch (typeStr) {
            case "AZTEC": return BarcodeType.AZTEC;
            case "CODABAR": return BarcodeType.CODABAR;
            case "CODE_39": return BarcodeType.CODE_39;
            case "CODE_93": return BarcodeType.CODE_93;
            case "DATA_MATRIX": return BarcodeType.DATA_MATRIX;
            case "EAN_8": return BarcodeType.EAN_8;
            case "EAN_13": return BarcodeType.EAN_13;
            case "ITF": return BarcodeType.ITF;
            case "PDF_417": return BarcodeType.PDF_417;
            case "QR_CODE": return BarcodeType.QR_CODE;
            case "UPC_A": return BarcodeType.UPC_A;
            case "UPC_E": return BarcodeType.UPC_E;
            default: return BarcodeType.CODE_128;
        }
    }

    private BarcodeFormat.Size toBarcodeSize(String sizeStr) {
        if (sizeStr == null) return BarcodeFormat.Size.FULL_PAPER;
        switch (sizeStr) {
            case "SMALL": return BarcodeFormat.Size.SMALL;
            case "HALF_PAPER": return BarcodeFormat.Size.HALF_PAPER;
            default: return BarcodeFormat.Size.FULL_PAPER;
        }
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

    // region ─── Printer.Listener ──────────────────────────────────────────────

    @Override
    public void onPrinterError(PrinterError printerError) {
        Log.e(TAG, "onPrinterError id=" + printerError.getRequestId()
                + " cause=" + printerError.getCause());
    }

    @Override
    public void onPrinterSuccessful(int printerRequestId) {
        Log.d(TAG, "onPrinterSuccessful id=" + printerRequestId);
    }

    // endregion
}
