package data;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Список валют .
 */

@Root(name = "ValCurs")
public class ValCurs
{
    @Attribute
    private String Date;
    @Attribute
    private String name;

    @ElementList(inline = true)
    private List<Valute> list;

    public List<Valute> getList()
    {
        return list;
    }
}
