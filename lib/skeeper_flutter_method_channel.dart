import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'models.dart';
import 'skeeper_flutter_platform_interface.dart';

/// An implementation of [SkeeperFlutterPlatform] that uses method channels.
class MethodChannelSkeeperFlutter extends SkeeperFlutterPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('skeeper_flutter');
  // Function(List<int> audioData)? _audioHandler;
  //
  // MethodChannelSkeeperFlutter() {
  //   methodChannel.setMethodCallHandler((call) async {
  //     if (call.method == 'onAudioData' && _audioHandler != null) {
  //       _audioHandler!(call.arguments as List<int>);
  //     }
  //   });
  // }

  @override
  Future<void> promptEnableBluetooth() async {
    await methodChannel.invokeMethod('promptEnableBluetooth');
  }

  @override
  Future<void> startBleScan() async {
    await methodChannel.invokeMethod('startBleScan');
  }

  @override
  Future<void> stopBleScan() async {
    await methodChannel.invokeMethod<String>('stopBleScan');
  }

  @override
  Future<Map<String, String>?> getSkeeperDeviceInfo() async {
    return await methodChannel
        .invokeMethod<Map<String, String>?>('getSkeeperDeviceInfo');
  }

  @override
  Future<bool> isSkeeperDevice() async {
    return (await methodChannel.invokeMethod<bool>('isSkeeperDevice')) ?? false;
  }

  @override
  Future<void> startSendingData() async {
    await methodChannel.invokeMethod('startSendingData');
  }

  @override
  Future<void> stopSendingData() async {
    await methodChannel.invokeMethod('stopSendingData');
  }

  @override
  Future<void> useSpeaker() async {
    await methodChannel.invokeMethod('useSpeaker');
  }

  @override
  Future<void> useEarpiece() async {
    await methodChannel.invokeMethod('useEarpiece');
  }

  @override
  Future<void> switchToHeartMode() async {
    await methodChannel.invokeMethod('switchToHeartMode');
  }

  @override
  Future<void> switchToFetusMode() async {
    await methodChannel.invokeMethod('switchToFetusMode');
  }

  @override
  Future<void> switchToLungsMode() async {
    await methodChannel.invokeMethod('switchToLungsMode');
  } //switchToLungsMode

  @override
  Future<void> connectToDevice(String address) async {
    await methodChannel
        .invokeMethod('connectToDevice', <String, dynamic>{'address': address});
  }

  @override
  Future<List<BluetoothDevice>> getBluetoothDevices() async {
    var list = await methodChannel.invokeMethod('getBluetoothDevices');
    return list
        .map<BluetoothDevice>((json) => BluetoothDevice.fromMap(json))
        .toList();
  }

  void setAudioDataHandler(Function(List<int> audioData) handler) {
    methodChannel.setMethodCallHandler((call) async {
      if (call.method == 'onAudioData') {
        // _audioHandler!(call.arguments as List<int>);
        handler(call.arguments as List<int>);
      }
    });
  }
}
