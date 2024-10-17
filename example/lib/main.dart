import 'dart:async';

import 'package:flutter/material.dart';
import 'package:skeeper_flutter/models.dart';
import 'package:skeeper_flutter/skeeper_flutter.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final skeeper = SkeeperFlutter();

  List<BluetoothDevice> list = [];

  Timer? timer1, timer2;

  @override
  void initState() {
    super.initState();
    //initPlatformState();
    skeeper.setAudioDataHandler((data) {
      print('usage => data received from audio handler => ${data.length}');
    });
  }

  void _stopRefreshing() {
    timer1?.cancel();
    timer2?.cancel();
    timer1 = null;
    timer2 = null;
    setState(() {});
  }

  void _startRefreshing() {
    timer1 = Timer.periodic(const Duration(seconds: 1), (timer) async {
      timer2 = timer;
      list = await skeeper.getBluetoothDevices();
      setState(() {});
    });

    if (list.isNotEmpty) _stopRefreshing();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
          appBar: AppBar(
            title: const Text('Plugin example app'), //skeeperfl_tag
          ),
          body: Column(
            children: [
              Expanded(
                  flex: 2,
                  child: Column(
                    children: [
                      SizedBox.fromSize(size: const Size(10, 10)),
                      TextButton(
                        onPressed: () async {
                          await skeeper.promptEnableBluetooth();
                          _stopRefreshing();
                        },
                        child: const Text('BLUE PERMISSIONS'),
                      ),
                      TextButton(
                        onPressed: () async {
                          await skeeper.startBleScan();
                          _startRefreshing();
                        },
                        child: const Text('START SCAN'),
                      ),
                      TextButton(
                        onPressed: () async {
                          await skeeper.stopBleScan();
                          _stopRefreshing();
                        },
                        child: const Text('STOP SCAN'),
                      ),
                      TextButton(
                        onPressed: () async {
                          _stopRefreshing();
                        },
                        child: const Text('STOP REFRESHING'),
                      ),
                      TextButton(
                        onPressed: () async {
                          _stopRefreshing();
                          await skeeper.useSpeaker();
                          await skeeper.startSendingData();
                        },
                        child: const Text('startSendingData'),
                      ),
                      TextButton(
                        onPressed: () async {
                          _stopRefreshing();
                          await skeeper.stopSendingData();
                        },
                        child: const Text('stopSendingData'),
                      ),
                      TextButton(
                        onPressed: () async {
                          _stopRefreshing();
                          await skeeper.switchToFetusMode();
                        },
                        child: const Text('switchToFetusMode'),
                      ),
                      TextButton(
                        onPressed: () async {
                          _stopRefreshing();
                          await skeeper.switchToHeartMode();
                        },
                        child: const Text('switchToHeartMode'),
                      ),
                      TextButton(
                        onPressed: () async {
                          _stopRefreshing();
                          await skeeper.switchToLungsMode();
                        },
                        child: const Text('switchToLungsMode'),
                      ),
                    ],
                  )),
              Expanded(
                child: ListView.builder(
                    itemCount: list.length,
                    itemBuilder: (ctx, pos) {
                      var dev = list[pos];
                      return TextButton(
                          onPressed: () async {
                            await skeeper.connectToDevice(dev.address);
                          },
                          child: Text('${dev.name} / ${dev.address}'));
                    }),
              ),
            ],
          )),
    );
  }
}
//skeeperfl_tag
