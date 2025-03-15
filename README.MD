# ImageNgine
## Image transformation service

This project is an image transformation service that allows users to upload images and apply transformations to them.
Modified pictures are either exposed as set of URLs to be embeddable e.g. in webpages, or the service can be used as a transformer using GraphQL API.

### Provider
Expose the modified images as a set of permalinks to be used in webpages.

### Transformer
GraphQL service to convert/transform the images realtime.

### Supported functions
- Resize
- Rotate
- Flip
- Crop
- Watermark
- Blur
- Brightness
- Contrast
- Grayscale
- Invert
- Sepia
- Stitch
- Text overlay
- Image overlay (sticker)

### Supported output formats
- JPEG
- PNG
- WEBP
- GIF
- BMP
- ICO


### Examples
#### Stitching three images using Transformer and adding a logo to the left bottom corner
```graphql
{
    transform {
        from(image: "https://localhost:8080/some-image1.jpg") {
            stitch(images: ["https://localhost:8080/some-image3.jpg", "https://localhost:8080/some-image2.jpg"])
            sticker(image: "https://localhost:8080/company-logo.jpg", x: 10, y: 10)
            cumulativeSink
        }
        to(format: PNG)
    }
}
```