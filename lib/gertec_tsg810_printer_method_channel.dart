import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'gertec_tsg810_printer_platform_interface.dart';
import 'src/gertec_types.dart';

class MethodChannelGertecTsg810Printer extends GertecTsg810PrinterPlatform {
  @visibleForTesting
  final methodChannel = const MethodChannel('gertec_tsg810_printer');

  @override
  Future<int> printText(
    String text, {
    required GertecAlign align,
    required bool bold,
    required bool italic,
    required bool underline,
    required int fontSize,
    required int lineSpacing,
  }) async {
    final args = <String, dynamic>{
      'text': text,
      'align': align.value,
      'bold': bold,
      'italic': italic,
      'underline': underline,
      'fontSize': fontSize,
      'lineSpacing': lineSpacing,
    };
    return await methodChannel.invokeMethod<int>('printText', {'textArgs': args}) ?? -1;
  }

  @override
  Future<int> feed(int lines) async {
    return await methodChannel.invokeMethod<int>('feed', {'lines': lines}) ?? -1;
  }

  @override
  Future<int> printBarcode(
    String data, {
    required GertecBarcodeType type,
    required GertecBarcodeSize size,
  }) async {
    final args = <String, dynamic>{
      'data': data,
      'type': type.value,
      'size': size.value,
    };
    return await methodChannel.invokeMethod<int>('printBarcode', {'barcodeArgs': args}) ?? -1;
  }

  @override
  Future<int> printImageBytes(
    Uint8List imageBytes, {
    required int width,
    required int height,
    required GertecAlign align,
    required bool autoResize,
  }) async {
    final args = <String, dynamic>{
      'imageBytes': imageBytes,
      'width': width,
      'height': height,
      'align': align.value,
      'autoResize': autoResize,
    };
    return await methodChannel.invokeMethod<int>('printImageBytes', {'imageArgs': args}) ?? -1;
  }

  @override
  Future<int> printImageUrl(String url) async {
    return await methodChannel.invokeMethod<int>('printImageUrl', {'url': url}) ?? -1;
  }

  @override
  Future<int> printHtml(String html) async {
    return await methodChannel.invokeMethod<int>('printHtml', {'html': html}) ?? -1;
  }

  @override
  Future<int> printOrder({
    required String dados,
    required int posicao,
    required int stilo,
    required int tamanho,
    required String data,
    required String senha,
    required String imageUrl,
  }) async {
    final args = <String, dynamic>{
      'dados': dados,
      'posicao': posicao,
      'stilo': stilo,
      'tamanho': tamanho,
      'data': data,
      'senha': senha,
      'imageUrl': imageUrl,
    };
    return await methodChannel.invokeMethod<int>('printOrder', {'ticketArgs': args}) ?? -1;
  }

  @override
  Future<int> printConsolidatedOrder({
    required List<Map<String, dynamic>> items,
    required String orderNumber,
    required bool takeHome,
    required String imageUrl,
  }) async {
    final args = <String, dynamic>{
      'items': items,
      'orderNumber': orderNumber,
      'takeHome': takeHome,
      'imageUrl': imageUrl,
    };
    return await methodChannel.invokeMethod<int>('printConsolidatedOrder', {'orderArgs': args}) ?? -1;
  }

  @override
  Future<int> getStatus() async {
    return await methodChannel.invokeMethod<int>('getStatus') ?? -1;
  }

  @override
  Future<int> getPaperUsage() async {
    return await methodChannel.invokeMethod<int>('getPaperUsage') ?? -1;
  }

  @override
  Future<int> resetPaperUsage() async {
    return await methodChannel.invokeMethod<int>('resetPaperUsage') ?? -1;
  }
}
