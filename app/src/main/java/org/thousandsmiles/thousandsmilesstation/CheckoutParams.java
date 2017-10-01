package org.thousandsmiles.thousandsmilesstation;

public class CheckoutParams {
    private int m_returnMonths;
    private String m_msg;

    public void setReturnMonths(int n)
    {
        m_returnMonths = n;
    }

    public void setMessage(String msg)
    {
        m_msg = msg;
    }

    public int getReturnMonths()
    {
        return m_returnMonths;
    }

    public String getMessage()
    {
        return m_msg;
    }
}
