package ige.integration.datasource;

import ige.integration.utils.XMLElementExtractor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TransactionDataSource extends AbstractReportDataSource {

    /**
	 * 
	 */
	private static final long serialVersionUID = 7791109254854964392L;
	double totalAmount;
	
	public double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public TransactionDataSource() {
        super(new String[]{"transactionDate", "transactionDescription", "transactionCharges", "credits"});
    }
	
	public TransactionDataSource(String xml,String cA, String depDa) {
        super(new String[]{"transactionDate", "transactionDescription", "transactionCharges", "credits"});
        setTransaction(xml,cA, depDa);
    }

    private void setTransaction(String xml,String creditAmount, String depDa){
        ArrayList<Row> rows = new ArrayList<Row>();
        String val = xml;
//        System.out.println("GGGGGGGGG: "+val.indexOf("<transactionses>"));
        int traverse = 0;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        totalAmount = 0.0;
        while(-1 != val.indexOf("<transactionses>",traverse)){
        	int ind1 = val.indexOf("<transactionses>",traverse);
        	int ind2 = val.indexOf("</transactionses>",traverse);
        	String v = val.substring(ind1,ind2);
        	v += "</transactionses>";
//        	val = val.substring(ind2+5);
        	traverse = ind2+5;
        	String tD = XMLElementExtractor.extractXmlElementValue(v, "transactionDate");
        	String tDate = "";
        	if(tD!=null){
        	Long timeStamp = Long.parseLong(tD);
        	 tDate= df.format(new Date(timeStamp*1000));
        	}
        	String charges = XMLElementExtractor.extractXmlElementValue(v, "charges");
        	totalAmount+=Double.parseDouble(charges);
			Row row = new Row(tDate, XMLElementExtractor.extractXmlElementValue(v, "description"), charges , "0");
            rows.add(row);
        }
        Row row = null;
		if(null != xml && xml.length() > 0){
        	row = new Row(depDa, "Credited Amount:", null, ""+totalAmount);
		}else{
			row = new Row(null, "no data found", null, null);
		}
        rows.add(row);
        setRows(rows);
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Row transaction = (Row) rows.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return transaction.getTransactionDate();

            case 1:
                return transaction.getTransactionDescription();

            case 2:
                return transaction.getTransactionCharges();

            case 3:
                return transaction.getCredits();
        }

        return null;
    }

    private class Row {

    	private String transactionDate;
    	private String transactionDescription;
    	private String transactionCharges;
    	private String credits;

        public Row() {
            super();
        }      
        

		public Row(String transactionDate, String transactionDescription,
				String transactionCharges, String credits) {
			super();
			this.transactionDate = transactionDate;
			this.transactionDescription = transactionDescription;
			this.transactionCharges = transactionCharges;
			this.credits = credits;
		}



		public String getTransactionDate() {
			return transactionDate;
		}

		public void setTransactionDate(String transactionDate) {
			this.transactionDate = transactionDate;
		}

		public String getTransactionDescription() {
			return transactionDescription;
		}

		public void setTransactionDescription(String transactionDescription) {
			this.transactionDescription = transactionDescription;
		}

		public String getTransactionCharges() {
			return transactionCharges;
		}

		public void setTransactionCharges(String transactionCharges) {
			this.transactionCharges = transactionCharges;
		}

		public String getCredits() {
			return credits;
		}

		public void setCredits(String credits) {
			this.credits = credits;
		}
        
    }
}