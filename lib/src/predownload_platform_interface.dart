import 'package:bccm_player/src/pigeon/predownload_pigeon.g.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class PredownloaderInterface extends PlatformInterface {
  static final Object _token = Object();

  static PredownloaderInterface _instance = PredownloaderInterface();

  static PredownloaderInterface get instance => _instance;

  final _pigeon = PredownloadPigeon();

  PredownloaderInterface() : super(token: _token);

  static set instance(PredownloaderInterface instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  void predownloadAndCache({
    required List<String> urls,
    bool shouldPreloadFirstSegment = true,
  }) {
    _pigeon.predownloadDownload(
      PredownloadConfig(
        urls: urls,
        shouldPreloadFirstSegment: shouldPreloadFirstSegment,
      ),
    );
  }
}
