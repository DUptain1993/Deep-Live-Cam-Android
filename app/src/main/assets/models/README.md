# Models Directory

**The app works without any model files.**

Face detection uses Google ML Kit (bundled automatically).
Face swap uses image-based warping + blending (no model needed).

## Optional: Higher-quality inference

Place `.tflite` model files here for improved face-swap quality:

- `inswapper_128_int8.tflite` — face swap model (converted from ONNX)
- `face_detector_fp16.tflite` — custom face detector (optional, ML Kit is used by default)
- `gfpgan_fp16.tflite` — face enhancement (optional)

### Converting ONNX models

```bash
pip install onnx onnx-tf tensorflow
python3 scripts/convert_onnx_to_tflite.py inswapper_128_fp16.onnx \
    --output app/src/main/assets/models/inswapper_128_int8.tflite
```
