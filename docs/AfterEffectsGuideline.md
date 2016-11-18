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
![Typical Layer](/docs/images/doc-ae-typical-layer.png)

- Please DO NOT scale, skew, rotate or set opacity under the shape's transformation. DO IT in layers Transform Section
![Layer Transform](/docs/images/doc-ae-layer-transform.png)

- Path trim is NOT supported
- Polystar is NOT supported
- Rectangles and Ellipses are NOT supported

### Image Layer (Experimental Feature)

- A layer simply backed by a PNG image is supported

## Animations

- expressions is NOT supported
- Only use interpolate type 'LINEAR' and 'BEZIER' on your keyframes
- DO NOT USE overshoot or undershoot bezier, in other words two control points should always stay in the red rectangle formed by two keyframes.
![Bezier Overshoot and Undershoot](/docs/images/doc-ae-wrong-bezier.png)

## Effects

### Gradient (Experimental Feature)

- LINEAR gradient supported by choosing 'Effect/Gradient Ramp'
- DO NOT USE 'Gradient Fill' 
![Right Linear Gradient](/docs/images/doc-ae-right-gradient.png)
![Wrong Linear Gradient](/docs/images/doc-ae-wrong-gradient.png)
