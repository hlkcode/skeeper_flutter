import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'models.dart';
import 'skeeper_flutter_method_channel.dart';

abstract class SkeeperFlutterPlatform extends PlatformInterface {
  /// Constructs a SkeeperFlutterPlatform.
  SkeeperFlutterPlatform() : super(token: _token);

  static final Object _token = Object();

  static SkeeperFlutterPlatform _instance = MethodChannelSkeeperFlutter();

  /// The default instance of [SkeeperFlutterPlatform] to use.
  ///
  /// Defaults to [MethodChannelSkeeperFlutter].
  static SkeeperFlutterPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [SkeeperFlutterPlatform] when
  /// they register themselves.
  static set instance(SkeeperFlutterPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<void> promptEnableBluetooth() {
    throw UnimplementedError(
        'promptEnableBluetooth() has not been implemented.');
  }

  Future<void> startBleScan() {
    throw UnimplementedError('startBleScan() has not been implemented.');
  }

  Future<void> stopBleScan() {
    throw UnimplementedError('stopBleScan() has not been implemented.');
  }

  Future<Map<String, String>?> getSkeeperDeviceInfo() {
    throw UnimplementedError(
        'getSkeeperDeviceInfo() has not been implemented.');
  }

  Future<bool> isSkeeperDevice() {
    throw UnimplementedError('isSkeeperDevice() has not been implemented.');
  }

  Future<void> startSendingData() {
    throw UnimplementedError('startSendingData() has not been implemented.');
  }

  Future<void> stopSendingData() {
    throw UnimplementedError('stopSendingData() has not been implemented.');
  }

  Future<void> useSpeaker() {
    throw UnimplementedError('useSpeaker() has not been implemented.');
  }

  Future<void> useEarpiece() {
    throw UnimplementedError('useEarpiece() has not been implemented.');
  }

  Future<void> switchToHeartMode() {
    throw UnimplementedError('switchToHeartMode() has not been implemented.');
  }

  Future<void> switchToFetusMode() {
    throw UnimplementedError('switchToFetusMode() has not been implemented.');
  }

  Future<void> switchToLungsMode() {
    throw UnimplementedError('switchToLungsMode() has not been implemented.');
  }

  Future<void> connectToDevice(String address) {
    throw UnimplementedError('connectToDevice() has not been implemented.');
  }

  Future<List<BluetoothDevice>> getBluetoothDevices() {
    throw UnimplementedError('getBluetoothDevices() has not been implemented.');
  }

  void setAudioDataHandler(Function(List<int> audioData) handler) {
    throw UnimplementedError('setAudioDataHandler() has not been implemented.');
  }
}
