# Model Conversion Guide

## Overview
This directory contains scripts to convert ONNX models to TensorFlow Lite for Android, optimized for **4GB RAM devices**.

## Prerequisites

```bash
pip install onnx onnx-tf tensorflow numpy pillow
```

## Quick Start

### 1. Download Original Models
Download the ONNX models from the original Deep Live Cam repository:

```bash
cd path/to/models
# Should contain:
# - inswapper_128_fp16.onnx (face swap model)
# - GFPGANv1.4.onnx (face enhancement model)
```

Or download from Hugging Face:
- https://huggingface.co/hacksider/deep-live-cam/resolve/main/inswapper_128_fp16.onnx
- https://huggingface.co/hacksider/deep-live-cam/resolve/main/GFPGANv1.4.onnx

### 2. Convert Models

```bash
cd scripts/

# Convert face swap model with INT8 quantization (recommended for 4GB RAM)
python3 convert_onnx_to_tflite.py \
    path/to/models/inswapper_128_fp16.onnx \
    --output ../app/src/main/assets/models/inswapper_128_int8.tflite

# Convert face enhancement model (FP16 for better quality)
python3 convert_onnx_to_tflite.py \
    path/to/models/GFPGANv1.4.onnx \
    --output ../app/src/main/assets/models/gfpgan_fp16.tflite \
    --no-quantize
```

## Quantization Options

### INT8 Quantization (Recommended for 4GB RAM)
- **Benefits**: 4x smaller model, 2-4x faster inference, lower memory usage
- **Tradeoff**: Slight accuracy loss (~1-2%)
- **Use for**: Face swap model (real-time processing)

```bash
python3 convert_onnx_to_tflite.py model.onnx --output model_int8.tflite
```

### FP16 Quantization (Balanced)
- **Benefits**: 2x smaller, minimal accuracy loss
- **Use for**: Face enhancement (quality matters)

```bash
python3 convert_onnx_to_tflite.py model.onnx --output model_fp16.tflite --no-quantize
```

### No Quantization (FP32)
- **Benefits**: Best accuracy
- **Tradeoff**: Largest size, slowest inference
- **Not recommended for 4GB RAM devices**

## Model Size Expectations

| Model | Original (ONNX) | INT8 (TFLite) | FP16 (TFLite) |
|-------|----------------|---------------|---------------|
| Face Swap | ~190 MB | ~48 MB | ~95 MB |
| GFPGAN | ~300 MB | ~75 MB | ~150 MB |

## Memory Budget (4GB RAM Device)

```
Total RAM: 4096 MB
Android System: ~1500 MB
App Memory Limit: ~512 MB
  ├─ Models: ~200 MB (1-2 models loaded)
  ├─ Bitmaps: ~100 MB (frames, preview)
  ├─ Native Heap: ~100 MB (camera, processing)
  └─ App Overhead: ~112 MB
```

## Testing Converted Models

1. Copy models to assets directory:
```bash
mkdir -p app/src/main/assets/models
cp *.tflite app/src/main/assets/models/
```

2. Build and install app:
```bash
cd ..
./gradlew installDebug
```

3. Test on device and verify:
   - Model loads successfully
   - Inference runs without OOM
   - Output quality is acceptable
   - Frame rate is 15+ FPS

## Troubleshooting

### Error: Out of Memory during conversion
- Use a machine with more RAM for conversion
- Conversion happens on desktop, not Android

### Error: Model too large for device
- Use INT8 quantization (smaller)
- Load only one model at a time
- Reduce input resolution

### Error: Accuracy loss too high
- Try FP16 instead of INT8
- Increase calibration dataset size
- Fine-tune post-quantization

### Error: Slow inference on device
- Enable GPU delegate (NNAPI)
- Reduce input resolution (640x480 instead of 720p)
- Use INT8 quantization

## Advanced: Custom Quantization

For better accuracy-performance tradeoff:

```python
# Modify convert_onnx_to_tflite.py
def representative_dataset():
    # Use real face images instead of random data
    import glob
    from PIL import Image
    
    for img_path in glob.glob("face_samples/*.jpg")[:100]:
        img = Image.open(img_path).resize((128, 128))
        img_array = np.array(img).astype(np.float32) / 255.0
        img_array = np.expand_dims(img_array, axis=0)
        yield [img_array]
```

## Next Steps

After conversion:
1. ✅ Models in `app/src/main/assets/models/`
2. ✅ Build Android app
3. ✅ Test face detection
4. ✅ Test face swap inference
5. ✅ Measure performance (FPS, memory)
6. ✅ Optimize if needed

## Support

For issues with model conversion:
- Check TensorFlow version compatibility
- Verify ONNX model is valid: `python3 -m onnx.checker model.onnx`
- Try conversion without quantization first
- Check conversion logs for specific errors

---

**Status**: Scripts ready, awaiting original ONNX models for conversion
