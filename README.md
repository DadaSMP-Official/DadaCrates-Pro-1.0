# DadaCrates Pro 1.0

Plugin crates completo per server Minecraft Paper/Spigot.

**DadaCrates Pro 1.0** permette di creare, gestire e aprire crate direttamente dal server con GUI, key, premi configurabili, probabilita, animazioni, suoni, hologrammi e permessi. La versione pubblicata qui e pensata per essere semplice da installare e pronta da provare: niente simboli obbligatori da resource pack, niente item strani di texture custom, solo crate leggibili e compatibili con un server vanilla/Paper.

## Versioni

- `1.0.0` - versione originale DadaSMP
- `1.0.1-HOSTMR` - versione GitHub/HostMR con crate default pulite, key GUI migliorata e fix per Paper moderno

## Funzioni principali

- GUI admin apribile con `/crate` o `/crates`
- crate default gia pronte: Comune, Rara, Epica, Leggendaria, Mitica, Voto e Oscura
- key per ogni crate con utilizzi configurabili
- menu dedicato per vedere tutte le key disponibili
- editor key con key normale, key x16, utilizzi e modalita pick
- premi configurabili con item, quantita, lore, comandi, chance e broadcast
- preview dei premi prima dell'apertura
- animazioni di apertura con suoni ed effetti
- supporto hologrammi sopra le crate
- particelle idle configurabili
- permessi per ogni crate
- blocchi crate protetti nel mondo
- configurazione tramite `config.yml`

## Comandi

- `/dadacrates`
- `/dcrates`
- `/crate`
- `/crates`
- `/lootcrate`
- `/loot`

## Permessi

- `dadacrates.admin` - modifica crate, key, reward e impostazioni
- `dadacrates.use` - permette di usare le crate
- `dadacrates.open.*` - permette di aprire tutte le crate
- `dadacrates.open.<crate>` - permette di aprire una crate specifica

## Download

Il jar compilato si trova qui:

`release/DadaCratesPro-1.0.1-HOSTMR.jar`

Per installarlo:

1. Scarica il jar.
2. Mettilo nella cartella `plugins` del server.
3. Riavvia il server.
4. Usa `/crates` o `/crate` in gioco.

## Compatibilita

- Testato su Paper `1.21.11`
- Compilato/testato con Java `21`
- Pensato per Paper/Spigot `1.20.1+`

## Supporto

Per supporto, bug o domande entra nel server Discord:

https://discord.gg/yXrDpKCGAs

## Note

Questa e la versione pubblica/GitHub del plugin. La build mantiene la base originale di DadaCratesPro, ma usa crate default pulite e leggibili senza dipendere da una resource pack.
