// 1 is full resolution. Adjust this for smaller outputs (e.g., 20 or higher for thumbnails)
def downsample = 3

// Retrieve all detections
def detections = getDetectionObjects()

// Ensure there are detections to process
if (detections == null || detections.isEmpty()) {
    print("No detections found. Exiting.")
    return
}

// Get image name and data
def imageData = getCurrentImageData()
def imageName = imageData.getServer().getMetadata().getName()


// Set up the output directory
def projectBaseDir = 'D:\\Clara\\Rapid and Slow Cohort\\SCANS+Qupath Projects\\HuD\\Qupath' // Replace with actual project directory
def pathOutput = buildFilePath(projectBaseDir, 'image_export')
mkdirs(pathOutput)

// Create a labeled image server for all detections
def cellLabelServer = new LabeledImageServer.Builder(imageData)
    .backgroundLabel(0, ColorTools.WHITE) // Specify background label (usually 0 or 255)
    .useDetections()                      // Use detections as labels
    .useInstanceLabels()                  // Use unique labels for each detection
    .downsample(downsample)               // Specify resolution for output
    .multichannelOutput(false)            // Single-channel output
    .build()
 
// Create a region request for the entire image
def requestROI = RegionRequest.createInstance(getCurrentImageData().getServer(), downsample)

try {
    // Rendered image with overlays as seen in the viewer
    def renderedOutput = buildFilePath(pathOutput, "${imageName}_rendered_downsample_${downsample}.tif")
    writeRenderedImageRegion(getCurrentViewer(), requestROI, renderedOutput)
    print("Rendered image saved to: ${renderedOutput}")

    // Binary mask for all detections
    //def binaryMaskOutput = buildFilePath(pathOutput, "${imageName}_binaryMask_downsample_${downsample}.tif")
    //writeImageRegion(cellLabelServer, requestROI, binaryMaskOutput)
    //print("Binary mask saved to: ${binaryMaskOutput}")
   
    // Original image without overlays
    def originalOutput = buildFilePath(pathOutput, "${imageName}_original_downsample_${downsample}.tif")
    writeImageRegion(getCurrentServer(), requestROI, originalOutput)
    print("Original image saved to: ${originalOutput}")
    
    def cellLabelsOutput = buildFilePath(pathOutput, "${imageName}_cellLabels_downsample_${downsample}.tif");
    writeImageRegion(cellLabelServer, requestROI, cellLabelsOutput);
    print("Cell labels saved to: ${cellLabelsOutput}")

    print("All detection outputs exported successfully to ${pathOutput}")
} catch (Exception e) {
    print("ERROR: Unable to write the outputs. Details: ${e.message}")
    e.printStackTrace()
}