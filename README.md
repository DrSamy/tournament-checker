# Tournament Checker

Utilitaire visant à simplifier la recherche de dates pour l'organisation de tournois de badminton en Bretagne.

Se base sur :
- [Le calendrier la ligue](https://bretagnebadminton.com/index.php/competitions/49-informations-competitions/1122-calendrier-des-competitions)
- [Le réglement de la commission ligue tournois](https://bretagnebadminton.com/index.php/competitions/49-informations-competitions/1295-tournois)

## Vérification de la disponibilité d'une date

`java -jar tournament-checker.jar -M N3 -m D8 -s 2025-02-22 -e 2025-02-23 -p 35400`

## Lister les weekends disponibles

`java -jar tournament-checker.jar -M N3 -m D8 -p 35400 -w`

## Arguments

M : Classement maximum

m : Classement minimum

s : Date de début

e : Date de fin

p : Code postal de la salle

w : Flag pour lister les weekends disponibles (ignore e et s)
