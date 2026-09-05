package persistencia;

/**
 * Un tramo de texto con formato.
 */
public class Run {

    private String texto;
    private String fuente;
    private int tamano;
    private boolean negrita;
    private boolean cursiva;
    private boolean subrayado;
    private boolean tachado;
    private int colorRGB;   // 0x00RRGGBB

    public Run() {
        this.texto = "";
        this.fuente = "Arial";
        this.tamano = 14;
        this.colorRGB = 0x000000;
    }

    public Run(String texto, String fuente, int tamano, int colorRGB,
               boolean negrita, boolean cursiva, boolean subrayado, boolean tachado) {
        this.texto = texto;
        this.fuente = fuente;
        this.tamano = tamano;
        this.colorRGB = colorRGB;
        this.negrita = negrita;
        this.cursiva = cursiva;
        this.subrayado = subrayado;
        this.tachado = tachado;
    }

    public String getTexto()          { return texto; }
    public void setTexto(String t)    { this.texto = t; }
    public String getFuente()         { return fuente; }
    public void setFuente(String f)   { this.fuente = f; }
    public int getTamano()            { return tamano; }
    public void setTamano(int t)      { this.tamano = t; }
    public boolean isNegrita()        { return negrita; }
    public void setNegrita(boolean b) { this.negrita = b; }
    public boolean isCursiva()        { return cursiva; }
    public void setCursiva(boolean b) { this.cursiva = b; }
    public boolean isSubrayado()      { return subrayado; }
    public void setSubrayado(boolean b){ this.subrayado = b; }
    public boolean isTachado()        { return tachado; }
    public void setTachado(boolean b) { this.tachado = b; }
    public int getColorRGB()          { return colorRGB; }
    public void setColorRGB(int c)    { this.colorRGB = c; }

    @Override
    public String toString() {
        return "Run{\"" + texto + "\", " + fuente + " " + tamano
                + ", color=#" + String.format("%06X", colorRGB & 0xFFFFFF) + "}";
    }
}
