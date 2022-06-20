package uni.fmi.management.http.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FileUploadResponse {
	@SerializedName("fileName")
	@Expose
    private String fileName;

	@SerializedName("downloadUri")
	@Expose
    private String downloadUri;

	@SerializedName("path")
	@Expose
    private String path;

	@SerializedName("size")
	@Expose
    private long size;
    
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getDownloadUri() {
		return downloadUri;
	}
	public void setDownloadUri(String downloadUri) {
		this.downloadUri = downloadUri;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
}