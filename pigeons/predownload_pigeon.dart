import 'package:pigeon/pigeon.dart';

// IMPORTANT INFORMATION
// This is a template pigeon file,
// After doing edits to this file you have to run pigeon to generate downloader_pigeon.g.dart:
//
// ```sh
// dart run pigeon --input pigeons/predownload_pigeon.dart
// ```
//
// See the "Contributing" docs for bccm_player for more info.

@ConfigurePigeon(PigeonOptions(
  dartOut: 'lib/src/pigeon/predownload_pigeon.g.dart',
  dartOptions: DartOptions(),
  javaOut:
      'android/src/main/java/media/bcc/bccm_player/pigeon/PredownloadApi.java',
  javaOptions: JavaOptions(package: 'media.bcc.bccm_player.pigeon'),
  swiftOut: 'ios/Classes/Pigeon/PredownloadApi.swift',
))
@HostApi()
abstract class PredownloadPigeon {
  @async
  @ObjCSelector("predownload:")
  void predownloadDownload(PredownloadConfig config);
}

class PredownloadConfig {
  late List<String?> urls;
  late bool shouldPreloadFirstSegment;
}
