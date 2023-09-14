# VOB

VOB is a small optimization mod for BTA

Dependent on hardware, this mod can offer a change in fps anywhere from reducing the framerate by ~20 to nearly tripling the framerate

What VOB does, is it swaps BTA from using drawLists (GL11 feature) to using VAOs (GL30 feature if I remember correctly)


Through tests haven't really been performed, but so far I've constructed this list of GPUs for whether VOB helps

```diff
+ iGPU: Intel(R) HD Graphics 530
+ dGPU: NVIDIA GeForce GTX 1650 (PCIe/SSE2)
- iGPU: AMD Radeon(TM) Graphics
- iGPU: Intel(R) UHD Graphics 770 (ADL-5 GT1)
```

Granted, I'm not sure yet if the GPU is actually the deciding factor, or if there's more important components to look at