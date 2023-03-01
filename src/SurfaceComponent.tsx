import { requireNativeComponent, Platform } from 'react-native';

const SurfaceComponent = requireNativeComponent(
  Platform.OS === 'ios' ? 'IARSurfaceView' : 'SurfaceView'
);

export default SurfaceComponent;
