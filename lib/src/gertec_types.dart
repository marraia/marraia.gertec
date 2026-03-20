class GertecAlign {
  const GertecAlign._internal(this.value);
  final int value;
  static const LEFT = GertecAlign._internal(0);
  static const CENTER = GertecAlign._internal(1);
  static const RIGHT = GertecAlign._internal(2);
}

class GertecBarcodeType {
  const GertecBarcodeType._internal(this.value);
  final String value;
  static const AZTEC = GertecBarcodeType._internal("AZTEC");
  static const CODABAR = GertecBarcodeType._internal("CODABAR");
  static const CODE_39 = GertecBarcodeType._internal("CODE_39");
  static const CODE_93 = GertecBarcodeType._internal("CODE_93");
  static const CODE_128 = GertecBarcodeType._internal("CODE_128");
  static const DATA_MATRIX = GertecBarcodeType._internal("DATA_MATRIX");
  static const EAN_8 = GertecBarcodeType._internal("EAN_8");
  static const EAN_13 = GertecBarcodeType._internal("EAN_13");
  static const ITF = GertecBarcodeType._internal("ITF");
  static const PDF_417 = GertecBarcodeType._internal("PDF_417");
  static const QR_CODE = GertecBarcodeType._internal("QR_CODE");
  static const UPC_A = GertecBarcodeType._internal("UPC_A");
  static const UPC_E = GertecBarcodeType._internal("UPC_E");
}

class GertecBarcodeSize {
  const GertecBarcodeSize._internal(this.value);
  final String value;
  static const SMALL = GertecBarcodeSize._internal("SMALL");
  static const HALF_PAPER = GertecBarcodeSize._internal("HALF_PAPER");
  static const FULL_PAPER = GertecBarcodeSize._internal("FULL_PAPER");
}
