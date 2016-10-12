# Keyframes Guideline on AfterEffects

## Software Version

- Adobe After Effects CC 2015


## Compositions

- pre-composition is NOT supported
- No restrictions on frame rate, but an integer frame rate is highly recommended

## Layers

### Layer Parenting

- Only NULL layers can be used as parent layer of other layers
- NULL layers can also be used as parent of other NULL layers

### Shape Layer
- Path merging is NOT supported.
    - At most one path per group
    - At most one stroke per group
    - At most one group per layer

- A typical shape layer looks like
* [Image placeholder for typical layer]

- Please DO NOT scale, skew, rotate or set opacity under the shape's transformation. DO IT in layers Transform Section
* [Image: https://fb.quip.com/-/blob/YcLAAApHX6u/nIuPdT0AXsAzVkx7StDo7A]

- Path trim is NOT supported
- Polystar is NOT supported
- Rectangles and Ellipses are NOT supported

### Image Layer (Experimental Feature)

- A layer simply backed by a PNG image is supported

## Animations

- expressions is NOT supported
- Only use interpolate type 'LINEAR' and 'BEZIER' on your keyframes
- DO NOT USE overshoot or undershoot bezier, in other words two control points should always stay in the red rectangle formed by two keyframes.
* [placeholder for overshoot]

## Effects

### Gradient (Experimental Feature)

- LINEAR gradient supported by choosing 'Effect/Gradient Ramp'
- DO NOT USE 'Gradient Fill' 
* [Image: https://fb.quip.com/-/blob/YcLAAApHX6u/pEpW8PGgXeON_h3a-q5tfA]
* [Image: https://fb.quip.com/-/blob/YcLAAApHX6u/8YPFHcHvFb1dT8iPZ_nfrg]
