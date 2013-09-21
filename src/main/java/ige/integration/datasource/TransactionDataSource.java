package ige.integration.datasource;

import ige.integration.utils.XMLElementExtractor;

import java.util.ArrayList;

public class TransactionDataSource extends AbstractReportDataSource {

    /**
	 * 
	 */
	private static final long serialVersionUID = 7791109254854964392L;

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
        System.out.println("GGGGGGGGG: "+val.indexOf("<guestTransactionses>"));
        while(-1 != val.indexOf("<guestTransactionses>")){
        	int ind1 = val.indexOf("<guestTransactionses>");
        	int ind2 = val.indexOf("</guestTransactionses>");
        	String v = val.substring(ind1,ind2);
        	v += "</guestTransactionses>";
        	val = val.substring(ind2+5);
        	String tD = XMLElementExtractor.extractXmlElementValue(v, "transactionDate");
        	String dd = null;
        	if(null != tD){
        		dd = tD.substring(0,tD.indexOf("T"));
        	}
        	Row row = new Row(dd, XMLElementExtractor.extractXmlElementValue(v, "description"), XMLElementExtractor.extractXmlElementValue(v, "charges"), XMLElementExtractor.extractXmlElementValue(v, "credits"));
            rows.add(row);
        }
        Row row = null;
		if(null != xml && xml.length() > 0){
        	row = new Row(depDa, "Credited Amount:", null, creditAmount);
		}else{
			row = new Row(null, "-", null, null);
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