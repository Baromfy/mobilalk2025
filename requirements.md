# Követelmények teljesítése

Ez a dokumentum részletezi, hogy a mobilalkalmazás hogyan teljesíti a meghatározott követelményeket.

## Firebase autentikáció

- **Regisztráció**: `RegisterActivity.java` - A felhasználók regisztrálhatnak
- **Bejelentkezés**: `MainActivity.java` - A felhasználók bejelentkezhetnek az alkalmazásba

## Adatmodell definiálása

- **Appointment osztály**: `app/src/main/java/com/example/mobilalk2025/model/Appointment.java` - Időpontfoglalások adatait tárolja és kezeli
- **NailService osztály**: `app/src/main/java/com/example/mobilalk2025/model/NailService.java` - A körmös szolgáltatások adatait tárolja és kezeli

## Activity-k használata

1. `MainActivity.java` - Bejelentkezési képernyő
2. `RegisterActivity.java` - Regisztrációs képernyő
3. `HomeActivity.java` - Kezdőképernyő menüvel
4. `ProfileActivity.java` - Profil kezelő képernyő
5. `ServiceListActivity.java` - Szolgáltatások listája
6. `AppointmentListActivity.java` - Időpontfoglalások listája
7. `NewAppointmentActivity.java` - Új időpontfoglalás létrehozása
8. `AppointmentDetailsActivity.java` - Időpontfoglalás részletei

## Beviteli mezők megfelelő típusa

- **Email bevitel**: `MainActivity.java` - Email típusú billentyűzet
- **Jelszó bevitel**: `MainActivity.java` - Kicsillagozott jelszó bevitel
- **Telefonszám bevitel**: `ProfileActivity.java` sor 52 - A phoneEditText beviteli mező telefonszám típusú
- **Név bevitel**: `ProfileActivity.java` sor 51 - A displayNameEditText beviteli mező szöveg típusú

## Layout típusok használata

- **ConstraintLayout**: 
  - `app/src/main/res/layout/activity_home.xml` - A főképernyő ConstraintLayout-ot használ
  - `app/src/main/res/layout/activity_profile.xml` - A profil képernyő ConstraintLayout-ot használ
  
- **ScrollView**: 
  - `app/src/main/res/layout/activity_profile.xml` - A profil képernyő ScrollView-t tartalmaz
  - `app/src/main/res/layout/activity_service_list.xml` - ScrollView használata
  
- **RecyclerView**:
  - `app/src/main/res/layout/activity_appointment_list.xml` - A foglalások listájának megjelenítéséhez
  - `app/src/main/res/layout/activity_service_list.xml` - A szolgáltatások listájának megjelenítéséhez

## Reszponzív megjelenítés

- **Különböző képernyőméretek**: 
  - `app/src/main/res/values-sw600dp/bools.xml` - Tablet-specifikus erőforrások
  - `app/src/main/res/values/bools.xml` - Általános beállítások különböző méretekhez
  
- **Elforgatás kezelése**: 
  - Az összes ConstraintLayout elrendezés és a százalékos méretek használata biztosítja az elforgatás esetén is megfelelő megjelenítést
  - `app/src/main/res/layout/activity_home.xml` - ScrollView használata a főképernyőn a landscape módban való görgetés biztosításához
  - `app/src/main/res/layout/activity_profile.xml`:
    * A teljes tartalom (beleértve a profilképet is) ScrollView-ban helyezkedik el a jobb landscape mód kezelésért
    * `fillViewport="true"`, `fadeScrollbars="false"` és `scrollbars="vertical"` attribútumok a ScrollView-n
    * Csak a cím és a mentés gomb marad fixálva a képernyőn
  - `app/src/main/res/layout/activity_main.xml` (Bejelentkezési képernyő):
    * A teljes tartalom ScrollView-ba került a jobb landscape mód kezelésért
    * `fillViewport="true"`, `fadeScrollbars="false"` és `scrollbars="vertical"` attribútumok a ScrollView-n
  - `app/src/main/res/layout/activity_register.xml` (Regisztrációs képernyő):
    * A teljes tartalom ScrollView-ba került a jobb landscape mód kezelésért
    * `fillViewport="true"`, `fadeScrollbars="false"` és `scrollbars="vertical"` attribútumok a ScrollView-n

## Animációk használata

1. **Fade In animáció**: 
   - `app/src/main/res/anim/fade_in.xml` - Fokozatos megjelenés
   - Használja: `HomeActivity.java` sorban 43-45 - A gombok animált megjelenítése

2. **Bounce animáció**: 
   - `app/src/main/res/anim/bounce.xml` - Ugráló animáció

3. **Slide animációk**:
   - `app/src/main/res/anim/slide_in_left.xml` és `slide_out_right.xml`
   - Használja: `ProfileActivity.java` sor 191-194 - A vissza gomb animációja
   - Használja: `HomeActivity.java` sorban 66-69 - Az activity váltáskor

## Intent-ek használata

- **Activity-k közötti navigáció**: 
  - `HomeActivity.java` sorok 58-70 - Navigáció a különböző képernyők között
  - `ProfileActivity.java` sor 37-44 - Visszatérés a bejelentkezési képernyőre, ha a felhasználó nincs bejelentkezve

## Lifecycle Hook használata

- **onResume**: `HomeActivity.java` sor 74-78 - Ellenőrzi az időpontokat, amikor az alkalmazás előtérbe kerül
- **onBackPressed**: `ProfileActivity.java` sor 190-194 - Egyéni animáció alkalmazása, amikor a felhasználó visszalép

## Android engedélyek és erőforrások használata

1. **Kamera hozzáférés**:
   - `AndroidManifest.xml` - Kamera engedély deklarálva
   - `ProfileActivity.java` sor 230-244 - Kamera engedély kérése és kezelése
   - `ProfileActivity.java` sor 251-273 - Kamera alkalmazás indítása és kép készítése

2. **Helymeghatározás (GPS/FINE_LOCATION) hozzáférés**:
   - `AndroidManifest.xml` - ACCESS_FINE_LOCATION engedély deklarálva
   - `ProfileActivity.java` sor 291-316 - Helymeghatározás engedély kérése és kezelése
   - `ProfileActivity.java` sor 323-341 - Helyadatok feldolgozása

3. **Internet hozzáférés**: 
   - `AndroidManifest.xml` - Internet engedély a Firebase kommunikációhoz

## Rendszerszolgáltatások használata

1. **Notification**:
   - `app/src/main/java/com/example/mobilalk2025/notification/AppointmentReminderReceiver.java` sorok 45-58 - Értesítés létrehozása és megjelenítése
   - `app/src/main/java/com/example/mobilalk2025/notification/AppointmentReminderReceiver.java` sorok 62-78 - Notification Channel létrehozása Android 8.0+ készülékekhez

2. **AlarmManager**:
   - `app/src/main/java/com/example/mobilalk2025/AppointmentListActivity.java` sorok 124-140 - Időzített emlékeztetők beállítása az AlarmManager segítségével
   - `app/src/main/java/com/example/mobilalk2025/AppointmentListActivity.java` sorok 131-136 - PendingIntent létrehozása az értesítéshez

## CRUD műveletek

- **Create**: 
  - `NewAppointmentActivity.java` - Új időpontfoglalás létrehozása
  - `ProfileActivity.java` sor 69-128 - Új felhasználói profil adatok létrehozása

- **Read**: 
  - `AppointmentListActivity.java` - Időpontfoglalások lekérdezése és listázása
  - `ServiceListActivity.java` - Szolgáltatások lekérdezése és listázása
  - `ProfileActivity.java` sor 69-99 - Felhasználói adatok betöltése

- **Update**: 
  - `ProfileActivity.java` sor 103-149 - Felhasználói adatok frissítése
  - `AppointmentDetailsActivity.java` - Időpontfoglalás részleteinek frissítése

- **Delete**: 
  - `AppointmentAdapter.java` - Időpontfoglalások törlése
  - `AppointmentDetailsActivity.java` - Időpontfoglalás törlése

## Komplex Firestore lekérdezések (index-mentes megoldások)

1. **Where feltétel és kliens oldali rendezés (Komplex lekérdezés 1)**: 
   - `app/src/main/java/com/example/mobilalk2025/AppointmentListActivity.java` sorok 91-93 és 105-107 - Felhasználóhoz tartozó időpontok lekérdezése és rendezése
   - A lekérdezés a currentUser.getUid() érték alapján szűri az időpontokat Firestore-ban
   - Az időpontokat növekvő sorrendben rendezi a dátum szerint, de kliens oldalon a Java Collections.sort segítségével
   ```java
   db.collection("appointments")
       .whereEqualTo("userId", currentUser.getUid())
       .get()
       

2. **Limitálás (Komplex lekérdezés 2)**: 
   - `app/src/main/java/com/example/mobilalk2025/ServiceListActivity.java` sorok 90-91 - Szolgáltatások számának korlátozása a lekérdezésben
   ```java
   db.collection("services")
       .limit(50)
   ```

3. **Egyszerű where feltétel (Komplex lekérdezés 3)**:
   - `app/src/main/java/com/example/mobilalk2025/AppointmentListActivity.java` sorok 119-137 - Egyszerű lekérdezés
   - Csak egy alap szűrés felhasználói azonosító alapján a Firestore-ban (whereEqualTo)
   - Egyszerű lista létrehozása az eredményekből
   ```java
   db.collection("appointments")
       .whereEqualTo("userId", currentUser.getUid())
       .get()
   ```
