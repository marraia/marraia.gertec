package br.com.marraia.gertec_tsg810_printer;

import android.app.Activity;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.HashMap;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

public class GertecTsg810PrinterPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {

    private MethodChannel channel;
    private Activity activity;
    private BinaryMessenger binaryMessenger;
    private GertecPrinter printer;

    // region ─── FlutterPlugin ─────────────────────────────────────────────────

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        binaryMessenger = flutterPluginBinding.getBinaryMessenger();
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        if (channel != null) {
            channel.setMethodCallHandler(null);
        }
    }

    // endregion

    // region ─── ActivityAware ─────────────────────────────────────────────────

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        // A impressora só é criada sob demanda (ensurePrinter): em aparelhos que não são Gertec
        // a GEDI não existe e inicializar aqui derrubaria o app.
        printer = null;
        channel = new MethodChannel(binaryMessenger, "gertec_tsg810_printer");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity();
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        onAttachedToActivity(binding);
    }

    @Override
    public void onDetachedFromActivity() {
        if (channel != null) {
            channel.setMethodCallHandler(null);
        }
        printer = null;
        activity = null;
    }

    // endregion

    // region ─── Detecção ──────────────────────────────────────────────────────

    private boolean initFailed = false;

    /** Aparelho Gertec (G800/GPOS800)? Só nele a GEDI existe. */
    static boolean isGertecDevice() {
        String manufacturer = Build.MANUFACTURER == null ? "" : Build.MANUFACTURER.toLowerCase();
        String brand = Build.BRAND == null ? "" : Build.BRAND.toLowerCase();
        return manufacturer.contains("gertec") || brand.contains("gertec");
    }

    /** Cria a impressora na primeira chamada; null se o aparelho não tiver a impressora Gertec. */
    private synchronized GertecPrinter ensurePrinter() {
        if (printer != null) return printer;
        if (activity == null || initFailed || !isGertecDevice()) return null;
        try {
            printer = new GertecPrinter(activity);
        } catch (Throwable t) {
            // GEDI ausente/incompatível: não tenta de novo a cada chamada
            initFailed = true;
            Log.e("gertec_tsg810", "GEDI init failed", t);
            printer = null;
        }
        return printer;
    }

    // endregion

    // region ─── MethodCallHandler ─────────────────────────────────────────────

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if ("isAvailable".equals(call.method)) {
            result.success(ensurePrinter() != null);
            return;
        }
        if (ensurePrinter() == null) {
            result.error("UNAVAILABLE", "Impressora Gertec não disponível neste aparelho.", null);
            return;
        }

        switch (call.method) {
            case "printText": {
                try {
                    HashMap textArgs = call.argument("textArgs");
                    result.success(printer.imprimirTexto(textArgs));
                } catch (Exception e) {
                    result.error("PRINT_ERROR", e.getMessage(), null);
                }
                break;
            }
            case "feed": {
                try {
                    int lines = call.argument("lines");
                    result.success(printer.avancarPapel(lines));
                } catch (Exception e) {
                    result.error("PRINT_ERROR", e.getMessage(), null);
                }
                break;
            }
            case "printBarcode": {
                try {
                    HashMap barcodeArgs = call.argument("barcodeArgs");
                    result.success(printer.imprimirCodigoBarras(barcodeArgs));
                } catch (Exception e) {
                    result.error("PRINT_ERROR", e.getMessage(), null);
                }
                break;
            }
            case "printImageBytes": {
                try {
                    HashMap imageArgs = call.argument("imageArgs");
                    result.success(printer.imprimirImagemBytes(imageArgs));
                } catch (Exception e) {
                    result.error("PRINT_ERROR", e.getMessage(), null);
                }
                break;
            }
            case "printImageUrl": {
                final String url = call.argument("url");
                new Thread(() -> {
                    try {
                        int res = printer.imprimirImagemUrl(url);
                        activity.runOnUiThread(() -> result.success(res));
                    } catch (Exception e) {
                        activity.runOnUiThread(() -> result.error("PRINT_ERROR", e.getMessage(), null));
                    }
                }).start();
                break;
            }
            case "printHtml": {
                final String html = call.argument("html");
                new Thread(() -> {
                    try {
                        int res = printer.imprimirHtml(html);
                        activity.runOnUiThread(() -> result.success(res));
                    } catch (Exception e) {
                        activity.runOnUiThread(() -> result.error("PRINT_ERROR", e.getMessage(), null));
                    }
                }).start();
                break;
            }
            case "printOrder": {
                final HashMap ticketArgs = call.argument("ticketArgs");
                new Thread(() -> {
                    try {
                        int res = printer.imprimirPedido(ticketArgs);
                        activity.runOnUiThread(() -> result.success(res));
                    } catch (Exception e) {
                        activity.runOnUiThread(() -> result.error("PRINT_ERROR", e.getMessage(), null));
                    }
                }).start();
                break;
            }
            case "printConsolidatedOrder": {
                final HashMap orderArgs = call.argument("orderArgs");
                new Thread(() -> {
                    try {
                        int res = printer.imprimirPedidoConsolidado(orderArgs);
                        activity.runOnUiThread(() -> result.success(res));
                    } catch (Exception e) {
                        activity.runOnUiThread(() -> result.error("PRINT_ERROR", e.getMessage(), null));
                    }
                }).start();
                break;
            }
            case "getStatus": {
                new Thread(() -> {
                    try {
                        int res = printer.obterStatus();
                        activity.runOnUiThread(() -> result.success(res));
                    } catch (Exception e) {
                        activity.runOnUiThread(() -> result.error("PRINT_ERROR", e.getMessage(), null));
                    }
                }).start();
                break;
            }
            case "getPaperUsage": {
                try {
                    result.success(printer.obterUsoPapel());
                } catch (Exception e) {
                    result.error("PRINT_ERROR", e.getMessage(), null);
                }
                break;
            }
            case "resetPaperUsage": {
                try {
                    result.success(printer.resetarUsoPapel());
                } catch (Exception e) {
                    result.error("PRINT_ERROR", e.getMessage(), null);
                }
                break;
            }
            default:
                result.notImplemented();
                break;
        }
    }

    // endregion
}
