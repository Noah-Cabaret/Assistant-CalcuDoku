import java.io.*;
import java.time.Instant;

public class Temps
{
	private Double tempsPrecedent;
	private Double debut;

	public Temps()
	{
		this.tempsPrecedent = 0.0;
		this.debut = 0.0;
	}

	public static Double maintenant()
	{
		Instant inst = Instant.now();
		return inst.getEpochSecond() + inst.getNano() / 1_000_000_000.0;
	}

	public Double getTempsPrecedent()
	{
		return this.tempsPrecedent;
	}

	public void setTempsPrecedent(Double newTempsPrecedent)
	{
		this.tempsPrecedent = newTempsPrecedent;
	}

	public Double tempsTotal()
	{
		return this.debut == 0.0 ? 0.0 : this.tempsPrecedent + Temps.maintenant() - this.debut;
	}

	public int heures()
	{
		return (tempsTotal().intValue() / 3600);
	}

	public int minutes()
	{
		return ((tempsTotal().intValue() / 60) % 60);
	}

	public int secondes()
	{
		return (tempsTotal().intValue() % 60);
	}

	public void lancer()
	{
		this.debut = Temps.maintenant();
	}

	public String toString()
	{
		return tempsTotal().toString();
	}
}
