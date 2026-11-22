import 'package:flutter/foundation.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'gertec_tsg810_printer_method_channel.dart';
import 'src/gertec_types.dart';

abstract class GertecTsg810PrinterPlatform extends PlatformInterface {
  GertecTsg810PrinterPlatform() : super(token: _token);

  static final Object _token = Object();
  static GertecTsg810PrinterPlatform _instance = MethodChannelGertecTsg810Printer();

  static GertecTsg810PrinterPlatform get instance => _instance;

  static set instance(GertecTsg810PrinterPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<bool> isAvailable() {
    throw UnimplementedError('isAvailable() has not been implemented.');
  }

  Future<int> printText(
    String text, {
    required GertecAlign align,
    required bool bold,
    required bool italic,
    required bool underline,
    required int fontSize,
    required int lineSpacing,
  }) {
    throw UnimplementedError('printText() has not been implemented.');
  }

  Future<int> feed(int lines) {
    throw UnimplementedError('feed() has not been implemented.');
  }

  Future<int> printBarcode(
    String data, {
    required GertecBarcodeType type,
    required GertecBarcodeSize size,
  }) {
    throw UnimplementedError('printBarcode() has not been implemented.');
  }

  Future<int> printImageBytes(
    Uint8List imageBytes, {
    required int width,
    required int height,
    required GertecAlign align,
    required bool autoResize,
  }) {
    throw UnimplementedError('printImageBytes() has not been implemented.');
  }

  Future<int> printImageUrl(String url) {
    throw UnimplementedError('printImageUrl() has not been implemented.');
  }

  Future<int> printHtml(String html) {
    throw UnimplementedError('printHtml() has not been implemented.');
  }

  Future<int> printOrder({
    required String dados,
    required int posicao,
    required int stilo,
    required int tamanho,
    required String data,
    required String senha,
    required String imageUrl,
  }) {
    throw UnimplementedError('printOrder() has not been implemented.');
  }

  Future<int> printConsolidatedOrder({
    required List<Map<String, dynamic>> items,
    required String orderNumber,
    required bool takeHome,
    required String imageUrl,
  }) {
    throw UnimplementedError('printConsolidatedOrder() has not been implemented.');
  }

  Future<int> getStatus() {
    throw UnimplementedError('getStatus() has not been implemented.');
  }

  Future<int> getPaperUsage() {
    throw UnimplementedError('getPaperUsage() has not been implemented.');
  }

  Future<int> resetPaperUsage() {
    throw UnimplementedError('resetPaperUsage() has not been implemented.');
  }
}
