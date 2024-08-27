package in._10h.java.springaurorafailover.standarddriver;

public class Test {
    private Integer id;
    private String textVal = "";
    private Integer intVal = 0;
    public Test() {/* do nothing */}
    public Integer getId() {
        return this.id;
    }
    public String getTextVal() {
        return this.textVal;
    }
    public Integer getIntVal() {
        return this.intVal;
    }
    public void setId(final Integer id) {
        this.id = id;
    }
    public void setTextVal(final String textVal) {
        this.textVal = textVal;
    }
    public void setIntVal(final Integer intVal) {
        this.intVal = intVal;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((textVal == null) ? 0 : textVal.hashCode());
        result = prime * result + ((intVal == null) ? 0 : intVal.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Test other = (Test) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (textVal == null) {
            if (other.textVal != null)
                return false;
        } else if (!textVal.equals(other.textVal))
            return false;
        if (intVal == null) {
            if (other.intVal != null)
                return false;
        } else if (!intVal.equals(other.intVal))
            return false;
        return true;
    }
    @Override
    public String toString() {
        return "Test [id=" + id + ", textVal=" + textVal + ", intVal=" + intVal + "]";
    }
}
