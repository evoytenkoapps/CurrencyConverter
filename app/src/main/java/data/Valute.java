package data;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Одна валюта
 */

@Root(name = "Valute")
public class Valute
{
    @Element(name = "NumCode")
    private String NumCode;

    @Element(name = "CharCode")
    private String CharCode;

    @Element(name = "Nominal")
    private double Nominal;

    // Название валюты
    @Element(name = "Name")
    private String Name;

    // Курс валюты
    @Element(name = "Value")
    private String Value;

    // Название валюты
    public String getName()
    {
        return Name;
    }

    // Курс валюты
    public String getValue()
    {
        return Value;
    }

    public double getNominal()
    {
        return Nominal;
    }

    @Override
    public String toString()
    {
        return Name + " " + Value + " руб.";
    }
}
