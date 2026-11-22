import 'package:flutter/material.dart';
import 'package:gertec_tsg810_printer/gertec_tsg810_printer.dart';

void main() {
  runApp(const GertecExampleApp());
}

class GertecExampleApp extends StatelessWidget {
  const GertecExampleApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Gertec TSG810 Example',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue),
        useMaterial3: true,
      ),
      home: const PrinterHomePage(),
    );
  }
}

class PrinterHomePage extends StatefulWidget {
  const PrinterHomePage({super.key});

  @override
  State<PrinterHomePage> createState() => _PrinterHomePageState();
}

class _PrinterHomePageState extends State<PrinterHomePage> {
  final _printer = GertecTsg810Printer();
  String _status = 'Aguardando...';
  bool _loading = false;

  // Font tester state
  final _fontTextController = TextEditingController(text: 'Texto de teste 123');
  double _fontSize = 18;
  bool _bold = false;
  bool _italic = false;
  bool _underline = false;
  GertecAlign _align = GertecAlign.LEFT;

  void _setStatus(String msg) => setState(() => _status = msg);
  void _setLoading(bool val) => setState(() => _loading = val);

  Future<void> _getStatus() async {
    _setLoading(true);
    try {
      final result = await _printer.getStatus();
      _setStatus('Status da impressora: $result');
    } catch (e) {
      _setStatus('Erro ao obter status: $e');
    } finally {
      _setLoading(false);
    }
  }

  Future<void> _printSimpleText() async {
    _setLoading(true);
    try {
      await _printer.printText(
        'Olá, Gertec TSG810!',
        align: GertecAlign.CENTER,
        bold: true,
        fontSize: 18,
      );
      await _printer.printText(
        'Texto normal alinhado à esquerda.',
        align: GertecAlign.LEFT,
        fontSize: 14,
      );
      await _printer.printText(
        'Texto em itálico e sublinhado.',
        italic: true,
        underline: true,
      );
      await _printer.feed(3);
      _setStatus('Texto impresso com sucesso.');
    } catch (e) {
      _setStatus('Erro ao imprimir texto: $e');
    } finally {
      _setLoading(false);
    }
  }

  Future<void> _printBarcode() async {
    _setLoading(true);
    try {
      await _printer.printText('Código de Barras:', align: GertecAlign.CENTER);
      await _printer.printBarcode(
        '1234567890',
        type: GertecBarcodeType.CODE_128,
        size: GertecBarcodeSize.FULL_PAPER,
      );
      await _printer.feed(2);
      await _printer.printText('QR Code:', align: GertecAlign.CENTER);
      await _printer.printBarcode(
        'https://flutter.dev',
        type: GertecBarcodeType.QR_CODE,
        size: GertecBarcodeSize.HALF_PAPER,
      );
      await _printer.feed(3);
      _setStatus('Códigos impressos com sucesso.');
    } catch (e) {
      _setStatus('Erro ao imprimir código: $e');
    } finally {
      _setLoading(false);
    }
  }

  Future<void> _printOrder() async {
    _setLoading(true);
    try {
      final n = DateTime.now();
      final dataFormatada = '${n.day.toString().padLeft(2, '0')}/${n.month.toString().padLeft(2, '0')}/${n.year} ${n.hour.toString().padLeft(2, '0')}:${n.minute.toString().padLeft(2, '0')}';
      await _printer.printOrder(
        dados: 'CACHORRO QUENTE',
        posicao: 1,
        stilo: 0,
        tamanho: 40,
        data: dataFormatada,
        senha: 'SENHA: 429988',
        imageUrl: '',
      );
      _setStatus('Pedido impresso com sucesso.');
    } catch (e) {
      _setStatus('Erro ao imprimir pedido: $e');
    } finally {
      _setLoading(false);
    }
  }

  Future<void> _printConsolidatedOrder() async {
    _setLoading(true);
    try {
      final items = [
        {
          'descricao': 'X-Burguer',
          'valor': 25.90,
          'ingredientes': ['Alface', 'Tomate', 'Queijo'],
          'removidos': ['Cebola'],
        },
        {
          'descricao': 'Fritas Grandes',
          'valor': 12.00,
          'ingredientes': <String>[],
          'removidos': <String>[],
        },
        {
          'descricao': 'Coca-Cola 500ml',
          'valor': 8.50,
          'ingredientes': <String>[],
          'removidos': <String>[],
        },
      ];
      await _printer.printConsolidatedOrder(
        items: items,
        orderNumber: '#0042',
        takeHome: false,
        imageUrl: '',
      );
      _setStatus('Pedido consolidado impresso com sucesso.');
    } catch (e) {
      _setStatus('Erro ao imprimir pedido consolidado: $e');
    } finally {
      _setLoading(false);
    }
  }

  Future<void> _printHtml() async {
    _setLoading(true);
    try {
      const html = '''
<html>
  <body>
    <h2 style="text-align:center">Recibo</h2>
    <p><b>Cliente:</b> João Silva</p>
    <p><b>Produto:</b> X-Burguer</p>
    <p><b>Valor:</b> R\$ 25,90</p>
    <hr>
    <p style="text-align:center">Obrigado pela preferência!</p>
  </body>
</html>
''';
      await _printer.printHtml(html);
      await _printer.feed(3);
      _setStatus('HTML impresso com sucesso.');
    } catch (e) {
      _setStatus('Erro ao imprimir HTML: $e');
    } finally {
      _setLoading(false);
    }
  }

  Future<void> _getPaperUsage() async {
    _setLoading(true);
    try {
      final usage = await _printer.getPaperUsage();
      _setStatus('Uso de papel: $usage');
    } catch (e) {
      _setStatus('Erro ao obter uso de papel: $e');
    } finally {
      _setLoading(false);
    }
  }

  Future<void> _testFont() async {
    _setLoading(true);
    try {
      await _printer.printText(
        _fontTextController.text.isEmpty ? 'Texto de teste' : _fontTextController.text,
        align: _align,
        bold: _bold,
        italic: _italic,
        underline: _underline,
        fontSize: _fontSize.round(),
      );
      await _printer.feed(2);
      _setStatus('Fonte testada: tamanho=${_fontSize.round()} bold=$_bold italic=$_italic underline=$_underline');
    } catch (e) {
      _setStatus('Erro ao testar fonte: $e');
    } finally {
      _setLoading(false);
    }
  }

  @override
  void dispose() {
    _fontTextController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Gertec TSG810'),
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
      ),
      body: ListView(
        padding: const EdgeInsets.all(12),
        children: [
          Card(
            child: Padding(
              padding: const EdgeInsets.all(10),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Status',
                    style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
                  ),
                  const SizedBox(height: 4),
                  Text(_status, style: const TextStyle(fontSize: 12)),
                  if (_loading) ...[
                    const SizedBox(height: 6),
                    const LinearProgressIndicator(),
                  ],
                ],
              ),
            ),
          ),
          const SizedBox(height: 10),
          _ActionButton(
            label: 'Status Impressora',
            icon: Icons.info_outline,
            onPressed: _loading ? null : _getStatus,
          ),
          const SizedBox(height: 6),
          _ActionButton(
            label: 'Uso de Papel',
            icon: Icons.receipt_long,
            onPressed: _loading ? null : _getPaperUsage,
          ),
          const SizedBox(height: 6),
          _ActionButton(
            label: 'Imprimir Texto',
            icon: Icons.text_fields,
            onPressed: _loading ? null : _printSimpleText,
          ),
          const SizedBox(height: 6),
          _ActionButton(
            label: 'Imprimir Código de Barras / QR',
            icon: Icons.qr_code,
            onPressed: _loading ? null : _printBarcode,
          ),
          const SizedBox(height: 6),
          _ActionButton(
            label: 'Imprimir Pedido',
            icon: Icons.receipt,
            onPressed: _loading ? null : _printOrder,
          ),
          const SizedBox(height: 6),
          _ActionButton(
            label: 'Pedido Consolidado',
            icon: Icons.list_alt,
            onPressed: _loading ? null : _printConsolidatedOrder,
          ),
          const SizedBox(height: 6),
          _ActionButton(
            label: 'Imprimir HTML',
            icon: Icons.code,
            onPressed: _loading ? null : _printHtml,
          ),
          const SizedBox(height: 12),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Testar Fonte',
                    style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
                  ),
                  const SizedBox(height: 8),
                  TextField(
                    controller: _fontTextController,
                    decoration: const InputDecoration(
                      labelText: 'Texto',
                      border: OutlineInputBorder(),
                      isDense: true,
                    ),
                    style: const TextStyle(fontSize: 13),
                  ),
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      const Text('Tamanho: ', style: TextStyle(fontSize: 12)),
                      Text('${_fontSize.round()}', style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                    ],
                  ),
                  Slider(
                    value: _fontSize,
                    min: 10,
                    max: 48,
                    divisions: 19,
                    label: '${_fontSize.round()}',
                    onChanged: _loading ? null : (v) => setState(() => _fontSize = v),
                  ),
                  Row(
                    children: [
                      const Text('Alinhamento:', style: TextStyle(fontSize: 12)),
                      const SizedBox(width: 8),
                      DropdownButton<GertecAlign>(
                        value: _align,
                        isDense: true,
                        style: const TextStyle(fontSize: 12, color: Colors.black87),
                        items: const [
                          DropdownMenuItem(value: GertecAlign.LEFT, child: Text('Esquerda')),
                          DropdownMenuItem(value: GertecAlign.CENTER, child: Text('Centro')),
                          DropdownMenuItem(value: GertecAlign.RIGHT, child: Text('Direita')),
                        ],
                        onChanged: _loading ? null : (v) => setState(() => _align = v!),
                      ),
                    ],
                  ),
                  Row(
                    children: [
                      Checkbox(
                        value: _bold,
                        onChanged: _loading ? null : (v) => setState(() => _bold = v!),
                      ),
                      const Text('Negrito', style: TextStyle(fontSize: 12)),
                      const SizedBox(width: 8),
                      Checkbox(
                        value: _italic,
                        onChanged: _loading ? null : (v) => setState(() => _italic = v!),
                      ),
                      const Text('Itálico', style: TextStyle(fontSize: 12)),
                      const SizedBox(width: 8),
                      Checkbox(
                        value: _underline,
                        onChanged: _loading ? null : (v) => setState(() => _underline = v!),
                      ),
                      const Text('Sublinhado', style: TextStyle(fontSize: 12)),
                    ],
                  ),
                  const SizedBox(height: 4),
                  SizedBox(
                    width: double.infinity,
                    child: ElevatedButton.icon(
                      onPressed: _loading ? null : _testFont,
                      icon: const Icon(Icons.print, size: 18),
                      label: const Text('Imprimir Teste', style: TextStyle(fontSize: 12)),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _ActionButton extends StatelessWidget {
  const _ActionButton({
    required this.label,
    required this.icon,
    required this.onPressed,
  });

  final String label;
  final IconData icon;
  final VoidCallback? onPressed;

  @override
  Widget build(BuildContext context) {
    return ElevatedButton.icon(
      onPressed: onPressed,
      icon: Icon(icon, size: 18),
      label: Text(label, style: const TextStyle(fontSize: 12)),
      style: ElevatedButton.styleFrom(
        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      ),
    );
  }
}
