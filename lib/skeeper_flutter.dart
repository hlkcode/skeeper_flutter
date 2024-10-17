import 'models.dart';
import 'skeeper_flutter_platform_interface.dart';

class SkeeperFlutter {
  // Future<String?> getPlatformVersion() {
  //   return SkeeperFlutterPlatform.instance.getPlatformVersion();
  // }

  Future<void> promptEnableBluetooth() {
    return SkeeperFlutterPlatform.instance.promptEnableBluetooth();
  }

  Future<void> startBleScan() {
    return SkeeperFlutterPlatform.instance.startBleScan();
  }

  Future<void> stopBleScan() {
    return SkeeperFlutterPlatform.instance.stopBleScan();
  }

  Future<Map<String, String>?> getSkeeperDeviceInfo() {
    return SkeeperFlutterPlatform.instance.getSkeeperDeviceInfo();
  }

  Future<bool> isSkeeperDevice() {
    return SkeeperFlutterPlatform.instance.isSkeeperDevice();
  }

  Future<void> startSendingData() {
    return SkeeperFlutterPlatform.instance.startSendingData();
  }

  Future<void> stopSendingData() {
    return SkeeperFlutterPlatform.instance.stopSendingData();
  }

  Future<void> useSpeaker() {
    return SkeeperFlutterPlatform.instance.useSpeaker();
  }

  Future<void> useEarpiece() {
    return SkeeperFlutterPlatform.instance.useEarpiece();
  }

  Future<void> switchToHeartMode() {
    return SkeeperFlutterPlatform.instance.switchToHeartMode();
  }

  Future<void> switchToFetusMode() {
    return SkeeperFlutterPlatform.instance.switchToFetusMode();
  }

  Future<void> switchToLungsMode() {
    return SkeeperFlutterPlatform.instance.switchToLungsMode();
  }

  Future<void> connectToDevice(String address) {
    return SkeeperFlutterPlatform.instance.connectToDevice(address);
  }

  Future<List<BluetoothDevice>> getBluetoothDevices() {
    return SkeeperFlutterPlatform.instance.getBluetoothDevices();
  }

  void setAudioDataHandler(Function(List<int> audioData) handler) {
    return SkeeperFlutterPlatform.instance.setAudioDataHandler(handler);
  }
}
