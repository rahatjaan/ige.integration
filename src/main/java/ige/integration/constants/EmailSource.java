package ige.integration.constants;

public class EmailSource {
	private String HOST;
	private String FROM_EMAIL;
	private String PASS;
	private String SUBJECT;
	private String MESSAGE;
	private String PORT;
	private String FILE_PATH;
	private String ADMIN_EMAIL;
	String FROM_NAME;
	public String getFROM_NAME() {
		return FROM_NAME;
	}
	public void setFROM_NAME(String fROM_NAME) {
		FROM_NAME = fROM_NAME;
	}
	public String getADMIN_EMAIL() {
		return ADMIN_EMAIL;
	}
	public void setADMIN_EMAIL(String aDMIN_EMAIL) {
		ADMIN_EMAIL = aDMIN_EMAIL;
	}
	public String getPASS() {
		return PASS;
	}
	public void setPASS(String pASS) {
		PASS = pASS;
	}
	public String getHOST() {
		return HOST;
	}
	public void setHOST(String hOST) {
		HOST = hOST;
	}
	public String getFROM_EMAIL() {
		return FROM_EMAIL;
	}
	public void setFROM_EMAIL(String fROM_EMAIL) {
		FROM_EMAIL = fROM_EMAIL;
	}
	public String getSUBJECT() {
		return SUBJECT;
	}
	public void setSUBJECT(String sUBJECT) {
		SUBJECT = sUBJECT;
	}
	public String getMESSAGE() {
		return MESSAGE;
	}
	public void setMESSAGE(String mESSAGE) {
		MESSAGE = mESSAGE;
	}
	public String getPORT() {
		return PORT;
	}
	public void setPORT(String pORT) {
		PORT = pORT;
	}
	public String getFILE_PATH() {
		return FILE_PATH;
	}
	public void setFILE_PATH(String fILE_PATH) {
		FILE_PATH = fILE_PATH;
	}
}
