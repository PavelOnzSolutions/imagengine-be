# ImageNgine
## Image transformation service

This project is an image transformation service that allows users to upload images and apply transformations to them.
Modified pictures are either exposed as set of URLs to be embeddable e.g. in webpages, or the service can be used as a transformer using GraphQL API.

### Provider
Expose the modified images as a set of permalinks to be used in webpages.

### Transformer
GraphQL service to convert/transform the images realtime.

### Supported functions
#### Transformations
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

#### Format conversion
Between any supported formats. Example: 
```graphql
{
    convert {
        from(images: ["https://localhost:8080/some-image1.png", "https://localhost:8080/some-image2.gif"])
        to(format: JPEG, quality: 80)
        cumulativeSink
    }
}
```

#### Histogram
Returns a histogram in PNG
```graphql
{
    histogram {
        from(image: "https://localhost:8080/some-image.jpg")
        cumulativeSink
    }
}
```

#### Image search
Returns an original image witrh marked objects plus their coordinates
```graphql
{
    find {
        what(object: "https://localhost:8080/some-image1.jpg", probability: 0.9)
        where(image: "https://localhost:8080/some-image2.jpg")
    }
}
```

### Supported output formats
- JPEG
- PNG
- WEBP
- GIF
- BMP
- ICO


### Examples
#### 1. Stitching three images using Transformer and adding a logo to the left bottom corner
```graphql
{
    transform {
        from(
              image: "https://localhost:8080/some-image1.jpg", 
              transformers: [STITCH, STICKER]
            ) {
            stitch(images: ["https://localhost:8080/some-image3.jpg", "https://localhost:8080/some-image2.jpg"])
            sticker(image: "https://localhost:8080/company-logo.jpg", x: 10, y: 10)
            cumulativeSink
        }
        to(format: JPEG)
    }
}
```

#### 2. Resize an image to 100x100 px and convert to grayscale using Transformer
```graphql
{
    transform {
        from(
              image: "https://localhost:8080/some-image.jpg", 
              transformers: [RESIZE, GRAYSCALE]
            ) {
            resize(width: 100, height: 100)
            grayscale
            cumulativeSink
        }
        to(format: PNG)
    }
}
```

