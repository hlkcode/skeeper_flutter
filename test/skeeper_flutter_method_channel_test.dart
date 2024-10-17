import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:skeeper_flutter/skeeper_flutter_method_channel.dart';

void main() {
  MethodChannelSkeeperFlutter platform = MethodChannelSkeeperFlutter();
  const MethodChannel channel = MethodChannel('skeeper_flutter');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    // expect(await platform.getPlatformVersion(), '42');
  });
}
