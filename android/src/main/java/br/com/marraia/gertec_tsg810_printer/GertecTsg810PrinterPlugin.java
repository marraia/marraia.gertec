package br.com.marraia.gertec_tsg810_printer;

import android.app.Activity;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.List;

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
    private GertecPrinter printer;
    private BinaryMessenger binaryMessenger;

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
        printer = new GertecPrinter(activity);
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

    // region ─── MethodCallHandler ─────────────────────────────────────────────

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (printer == null) {
            result.error("NO_ACTIVITY", "Printer not initialized.", null);
            return;
        }

        switch (call.method) {
            case "printText": {
                HashMap textArgs = call.argument("textArgs");
                result.success(printer.imprimirTexto(textArgs));
                break;
            }
            case "feed": {
                int lines = call.argument("lines");
                result.success(printer.avancarPapel(lines));
                break;
            }
            case "printBarcode": {
                HashMap barcodeArgs = call.argument("barcodeArgs");
                result.success(printer.imprimirCodigoBarras(barcodeArgs));
                break;
            }
            case "printImageBytes": {
                HashMap imageArgs = call.argument("imageArgs");
                result.success(printer.imprimirImagemBytes(imageArgs));
                break;
            }
            case "printImageUrl": {
                final String url = call.argument("url");
                new Thread(() -> {
                    int res = printer.imprimirImagemUrl(url);
                    activity.runOnUiThread(() -> result.success(res));
                }).start();
                break;
            }
            case "printHtml": {
                final String html = call.argument("html");
                new Thread(() -> {
                    int res = printer.imprimirHtml(html);
                    activity.runOnUiThread(() -> result.success(res));
                }).start();
                break;
            }
            case "printOrder": {
                final HashMap ticketArgs = call.argument("ticketArgs");
                new Thread(() -> {
                    int res = printer.imprimirPedido(ticketArgs);
                    activity.runOnUiThread(() -> result.success(res));
                }).start();
                break;
            }
            case "printConsolidatedOrder": {
                final HashMap orderArgs = call.argument("orderArgs");
                new Thread(() -> {
                    int res = printer.imprimirPedidoConsolidado(orderArgs);
                    activity.runOnUiThread(() -> result.success(res));
                }).start();
                break;
            }
            case "getStatus": {
                result.success(printer.obterStatus());
                break;
            }
            case "getPaperUsage": {
                result.success(printer.obterUsoPapel());
                break;
            }
            case "resetPaperUsage": {
                result.success(printer.resetarUsoPapel());
                break;
            }
            default:
                result.notImplemented();
                break;
        }
    }

    // endregion
}
