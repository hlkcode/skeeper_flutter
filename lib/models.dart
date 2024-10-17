import 'dart:convert';

class BluetoothDevice {
  BluetoothDevice({
    required this.name,
    required this.address,
    required this.alias,
  });

  final String name;
  final String address;
  final String alias;

  factory BluetoothDevice.fromJson(String str) =>
      BluetoothDevice.fromMap(json.decode(str));

  String toJson() => json.encode(toMap());

  factory BluetoothDevice.fromMap(Map<dynamic, dynamic> json) =>
      BluetoothDevice(
        name: json["name"],
        address: json["address"],
        alias: json["alias"],
      );

  Map<String, dynamic> toMap() => {
        "name": name,
        "address": address,
        "alias": alias,
      };
}
