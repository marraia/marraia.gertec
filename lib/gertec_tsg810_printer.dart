import 'dart:typed_data';

import 'gertec_tsg810_printer_platform_interface.dart';
import 'src/gertec_types.dart';

export 'src/gertec_types.dart';

class GertecTsg810Printer {
  Future<int> printText(
    String text, {
    GertecAlign align = GertecAlign.LEFT,
    bool bold = false,
    bool italic = false,
    bool underline = false,
    int fontSize = 14,
    int lineSpacing = 0,
  }) {
    return GertecTsg810PrinterPlatform.instance.printText(
      text,
      align: align,
      bold: bold,
      italic: italic,
      underline: underline,
      fontSize: fontSize,
      lineSpacing: lineSpacing,
    );
  }

  Future<int> feed(int lines) {
    return GertecTsg810PrinterPlatform.instance.feed(lines);
  }

  Future<int> printBarcode(
    String data, {
    GertecBarcodeType type = GertecBarcodeType.CODE_128,
    GertecBarcodeSize size = GertecBarcodeSize.FULL_PAPER,
  }) {
    return GertecTsg810PrinterPlatform.instance.printBarcode(
      data,
      type: type,
      size: size,
    );
  }

  Future<int> printImageBytes(
    Uint8List imageBytes, {
    int width = 0,
    int height = 0,
    GertecAlign align = GertecAlign.CENTER,
    bool autoResize = true,
  }) {
    return GertecTsg810PrinterPlatform.instance.printImageBytes(
      imageBytes,
      width: width,
      height: height,
      align: align,
      autoResize: autoResize,
    );
  }

  Future<int> printImageUrl(String url) {
    return GertecTsg810PrinterPlatform.instance.printImageUrl(url);
  }

  Future<int> printHtml(String html) {
    return GertecTsg810PrinterPlatform.instance.printHtml(html);
  }

  Future<int> printOrder({
    required String dados,
    required int posicao,
    required int stilo,
    required int tamanho,
    required String data,
    String senha = "",
    required String imageUrl,
  }) {
    return GertecTsg810PrinterPlatform.instance.printOrder(
      dados: dados,
      posicao: posicao,
      stilo: stilo,
      tamanho: tamanho,
      data: data,
      senha: senha,
      imageUrl: imageUrl,
    );
  }

  Future<int> printConsolidatedOrder({
    required List<Map<String, dynamic>> items,
    required String orderNumber,
    required bool takeHome,
    required String imageUrl,
  }) {
    return GertecTsg810PrinterPlatform.instance.printConsolidatedOrder(
      items: items,
      orderNumber: orderNumber,
      takeHome: takeHome,
      imageUrl: imageUrl,
    );
  }

  Future<int> getStatus() {
    return GertecTsg810PrinterPlatform.instance.getStatus();
  }

  Future<int> getPaperUsage() {
    return GertecTsg810PrinterPlatform.instance.getPaperUsage();
  }

  Future<int> resetPaperUsage() {
    return GertecTsg810PrinterPlatform.instance.resetPaperUsage();
  }
}
