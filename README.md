# gertec_tsg810_printer

Plugin Flutter (Android) para a impressora térmica interna dos POS **Gertec GPOS800 / Smart G800**, usando a biblioteca **GEDI** (`android/libs/libgedi-0.190121.gpos800.aar`).

## Instalação

```yaml
dependencies:
  gertec_tsg810_printer:
    git:
      url: https://github.com/marraia/marraia.gertec.git
      ref: main
```

O AAR é declarado por nome (`flatDir "libs"`), então o app também precisa de uma cópia em `android/app/libs/libgedi-0.190121.gpos800.aar`.

> Se o app também usar o plugin da Elgin (`minipdvm8`), as classes `com.xcheng.printerservice.*` existem nos dois AARs (são idênticas). Use no app uma cópia do `libgedi` sem o pacote `com/xcheng/` dentro do `classes.jar`, senão o build falha em `checkDebugDuplicateClasses`.

## Uso

```dart
final printer = GertecTsg810Printer();

if (await printer.isAvailable()) {      // false em aparelhos que não são Gertec, sem lançar erro
  await printer.printText('Olá, Gertec!', align: GertecAlign.CENTER, bold: true, fontSize: 24);
  await printer.feed(3);
}
```

A GEDI só é inicializada na primeira chamada e somente quando o fabricante do aparelho é Gertec, então o plugin pode ficar no mesmo app que roda em outros POS (ex.: Elgin M10).

| Método | Retorno |
|---|---|
| `isAvailable()` | `true` se o aparelho é Gertec e a impressora inicializou |
| `printText`, `printBarcode`, `printImageBytes`, `printImageUrl`, `feed` | `0` sucesso, `-1` erro |
| `printOrder`, `printConsolidatedOrder` | cupom de pedido (individual / consolidado) |
| `getStatus()` | `0` OK · `1` superaquecida · `2` sem papel · `3` erro |
| `getPaperUsage()`, `resetPaperUsage()` | contador de papel |

O app de exemplo fica em `example/`.
