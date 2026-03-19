#!/usr/bin/env python3
"""
ONNX to TensorFlow Lite Converter (optional helper script).

The Android app does NOT require this script to run.
Face swap works out of the box using ML Kit face detection + image blending.
This script is only needed if you want to provide .tflite model files for
higher-quality inference.

Usage:
    pip install onnx onnx-tf tensorflow
    python3 scripts/convert_onnx_to_tflite.py inswapper_128_fp16.onnx \
        --output app/src/main/assets/models/inswapper_128_int8.tflite
"""

import sys
import os
import argparse

def check_deps():
    missing = []
    for mod in ("onnx", "onnx_tf", "tensorflow"):
        try:
            __import__(mod)
        except ImportError:
            missing.append(mod.replace("_", "-"))
    if missing:
        print(f"Missing packages: {', '.join(missing)}")
        print("Install with:  pip install onnx onnx-tf tensorflow")
        sys.exit(1)

def main():
    parser = argparse.ArgumentParser(description="Convert ONNX to TFLite for Android")
    parser.add_argument("onnx_model", help="Path to ONNX model file")
    parser.add_argument("--output", "-o", help="Output TFLite file path")
    parser.add_argument("--no-quantize", action="store_true", help="Skip INT8 quantisation")
    parser.add_argument("--temp-dir", default="./temp_tf_model")
    args = parser.parse_args()

    check_deps()

    import onnx
    from onnx_tf.backend import prepare
    import tensorflow as tf
    import numpy as np

    # Step 1: ONNX -> TF SavedModel
    print(f"Loading ONNX model: {args.onnx_model}")
    onnx_model = onnx.load(args.onnx_model)
    tf_rep = prepare(onnx_model)
    tf_rep.export_graph(args.temp_dir)
    print(f"TF SavedModel exported to {args.temp_dir}")

    # Step 2: TF SavedModel -> TFLite
    converter = tf.lite.TFLiteConverter.from_saved_model(args.temp_dir)

    if not args.no_quantize:
        print("Applying INT8 quantisation...")
        converter.optimizations = [tf.lite.Optimize.DEFAULT]
        converter.target_spec.supported_ops = [
            tf.lite.OpsSet.TFLITE_BUILTINS_INT8,
            tf.lite.OpsSet.TFLITE_BUILTINS,
        ]

        def representative_dataset():
            for _ in range(100):
                yield [np.random.rand(1, 128, 128, 3).astype(np.float32)]

        converter.representative_dataset = representative_dataset
        converter.inference_input_type = tf.int8
        converter.inference_output_type = tf.int8

    tflite_model = converter.convert()

    output_path = args.output
    if not output_path:
        base = os.path.splitext(os.path.basename(args.onnx_model))[0]
        suffix = "_int8" if not args.no_quantize else "_fp32"
        output_path = f"{base}{suffix}.tflite"

    os.makedirs(os.path.dirname(output_path) or ".", exist_ok=True)
    with open(output_path, "wb") as f:
        f.write(tflite_model)

    size_mb = os.path.getsize(output_path) / (1024 * 1024)
    print(f"Done: {output_path} ({size_mb:.1f} MB)")

if __name__ == "__main__":
    main()
