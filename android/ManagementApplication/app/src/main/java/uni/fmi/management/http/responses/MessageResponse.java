package uni.fmi.management.http.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MessageResponse {
	@SerializedName("message")
	@Expose
	private String message;

	public MessageResponse() {
	}

	public MessageResponse(String message) {
		this.message = message;
	}

	public String getMessage() { return message; }

	public void setMessage(String message) {
		this.message = message;
	}
}