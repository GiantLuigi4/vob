# VOB

VOB is a small optimization mod for BTA

Dependent on hardware, this mod can offer a change in fps anywhere from reducing the framerate by ~20 to nearly tripling the framerate

What VOB does, is it swaps BTA from using drawLists (GL11 feature) to using VAOs (GL30 feature if I remember correctly)

# VAO's

Through tests haven't really been performed, but so far I've constructed this list of GPUs for whether VOB helps

```diff
NVIDIA:
+ dGPU: NVIDIA GeForce GTX 1650 (PCIe/SSE2)
+ dGPU: (with batching) NVIDIA GeForce RTX 3080 (PCIe/SSE2)
= NVIDIA GeForce 9600 GT (PCIe/SSE)

Intel:
+ iGPU: (my laptop's GPU, gets nearly a 3x performance boost) Intel(R) HD Graphics 530
+ iGPU: Intel(R) UHD Graphics 610 (CFL GT1)
- iGPU: Intel(R) UHD Graphics 770 (ADL-5 GT1)
```

Summary:
- Intel: Most known GPUs benefit from it, it seems like there's gonna be a cutoff point somewhere between 610 and 770
- AMD: Nothing is known for AMD
- NVIDIA: so far all NVIDIA GPUs benefit from it (or have remained at pretty much the same performance, with maybe a slight improvement)
- Steamdeck: gains ~20 fps, however batching hurts performance

# Instanced Entity Rendering
An upcoming feature of VOB, is instanced entity rendering

This can bring the game from ~16 fps with 500 pigs to ~25 with 600 or ~28 with 500
