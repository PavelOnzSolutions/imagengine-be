# ImagEngine
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
query ConversionQueryExample {
    convert(images: {
        from: {
            imageList: ["https://someimageservice.com/image.jpg", "https://someimageservice.com/image23452.png"]
        },
        to: {
            format: GIF
        }
    })
    {
       urls
       success
    }
}
```

#### Histogram
Returns a histogram as numbers and URL to PNG graph 
```graphql
query GetImageHistogram {
    histogram(
        images: {
            imageList:  ["https://someimageservice.com/image.jpg", "https://someimageservice.com/image23452.png"]
    })
    {
        image
        histogram
        url
    }
}
```

#### Image search
Returns an original image with marked objects plus their planar coordinates, pointing to the center of the object found in the image.
```graphql
query FindFaceQuery {
    search(
        in: {
            what: {
                object: "https://someimageservice.com/PetersIDPhoto.jpg",
                probability: 0.8
            },
            where: {
                image: "https://someimageservice.com/teambuildingPhoto04.jpg" 
            }
        } 
    ) {
        success
        coordinates {
            x
            y
        }
        markedImage
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
#### 1. Stitching three images using Transformer and adding a logo (in its original size, as width/height spec is missing), to the left bottom corner
```graphql
query MyStitchingQuery {
    transform(
        image: {
            from: {
                source: "https://someimageservice.com/image.jpg",
                transformers: [STITCH, STICKER],
                stitch: {
                    images: ["https://someimageservice.com/image34.jpg", "https://someimageservice.com/image11.jpg"],
                    direction: HORIZONTAL
                },
                sticker: {
                    image: "https://mycompany/logo.png",
                    x: 10,
                    y: 10,
                }
            },
            to: {
                format: PNG
            }
        }
    ) {
        url
        success
        message
    }
}
```

#### 2. Resize an image to 100x100 px and convert to grayscale using Transformer. Notice the Grayscale filter does not need any additional parameters.
```graphql
query MyResizeQuery {
    transform(
        image: {
            from: {
                source: "https://someimageservice.com/image.jpg",
                transformers: [RESIZE, GRAYSCALE],
                resize: {
                    width: 100,
                    height: 100
                }
            },
            to: {
                format: PNG
            }
        }
    ) {
        url
        success
        message
    }
}
```

