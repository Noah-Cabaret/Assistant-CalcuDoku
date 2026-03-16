package fr.univ.calcudoku.save;

import java.time.Instant;

public class Temps
{
	/* Attributs :
	 * debut : timestamp enregistré au lancement de la grille
	 * tempsPrecedent : temps passé sur une grille déjà commencée que
	 * le joueur souhaite continuer
	 */

	private Double debut;
	private Double tempsPrecedent;

	public Temps()
	{
		this.tempsPrecedent = 0.0;
		this.debut = 0.0;
	}

	/* Méthode de classe pour obtenir le timestamp décimal depuis l'epoch Unix
	 * (1er janvier 1970 à 0h00 heure britannique)
	 */

	public static Double maintenant()
	{
		Instant inst = Instant.now();
		return inst.getEpochSecond() + inst.getNano() / 1_000_000_000.0;
	}

	/* Méthodes get() et set() */

	public Double getTempsPrecedent()
	{
		return this.tempsPrecedent;
	}

	public void setTempsPrecedent(Double newTempsPrecedent)
	{
		this.tempsPrecedent = newTempsPrecedent;
	}

	/* Temps total passé sur la grille
	 * Sécurité : renvoie 0.0 si le timer n'a pas été lancé pour ne pas juste
	 * retourner Temps.maintenant() + tempsPrecedent
	 */

	public Double tempsTotal()
	{
		return this.debut == 0.0 ? 0.0 : this.tempsPrecedent + Temps.maintenant() - this.debut;
	}

	/* Conversion en unités de temps pour simplifier
	 * l'affichage graphique
	 */

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

	/* Simple enregistrement du timestamp pour lancer le timer */

	public void lancer()
	{
		this.debut = Temps.maintenant();
	}

	/* Conversion en String pour la sauvegarde */

	public String toString()
	{
		return tempsTotal().toString();
	}
}
