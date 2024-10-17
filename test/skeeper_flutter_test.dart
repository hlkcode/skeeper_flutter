import 'package:flutter_test/flutter_test.dart';
import 'package:skeeper_flutter/skeeper_flutter.dart';
import 'package:skeeper_flutter/skeeper_flutter_method_channel.dart';
import 'package:skeeper_flutter/skeeper_flutter_platform_interface.dart';

// class MockSkeeperFlutterPlatform
//     with MockPlatformInterfaceMixin
//     implements SkeeperFlutterPlatform {
//   @override
//   Future<String?> getPlatformVersion() => Future.value('42');
// }

void main() {
  final SkeeperFlutterPlatform initialPlatform =
      SkeeperFlutterPlatform.instance;

  test('$MethodChannelSkeeperFlutter is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelSkeeperFlutter>());
  });

  test('getPlatformVersion', () async {
    SkeeperFlutter skeeperFlutterPlugin = SkeeperFlutter();
    // MockSkeeperFlutterPlatform fakePlatform = MockSkeeperFlutterPlatform();
    // SkeeperFlutterPlatform.instance = fakePlatform;

    // expect(await skeeperFlutterPlugin.getPlatformVersion(), '42');
  });
}
