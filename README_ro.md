# APD - Tema #1b

## Thread Pool - Performance Improvements

Folosirea unui thread pool aduce o serie de avantaje în ceea ce privește performanța aplicației:
- dimensiunea limitată a thread pool-ului permite controlul numărului de fire de execuție care rulează în același timp - astfel, **este evitat overhead-ul** generat în cazul supraîncărcării sistemului;
- reutilizarea firelor de execuție **elimină costul de creare și distrugere a acestora**, atât din punctul de vedere al timpului, cât și al resurselor necesare;

Operația de extragere a unui task din coadă a fost sincronizată cu ajutorul unui _semafor_, astfel încât thread-urile de tip Worker sunt notificate imediat ce un task este disponibil. Acest lucru **reduce timpul de așteptare al thread-urilor**, prevenind, totodată, accesul concurent la coadă - în plus, distribuția task-urilor este **echitabilă și uniformă**.

_Obs:_ În cazul în care coada de task-uri este goală, dar submisiile nu au fost încă finalizate, thread-urile vor aștepta semnalizarea unui nou task.

Contextul aplicației (i.e. accesarea și actualizarea intrărilor dintr-o bază de date) justifică utilitatea unui thread pool:
- operațiile de citire și scriere pot avea drept target intrări independente din baza de date, ceea ce permite paralelizarea acestora;
  - în cazul în care acestea din urmă au drept obiect intrări diferite (nefiind nevoie de sincronizare), **se evită blocarea** thread-ului principal, care ar trebui să aștepte finalizarea fiecărei operații în parte.
  - în cazul în care operațiile țintesc aceeași intrare, **se evită accesul concurent** la aceasta, fiind implementate mecanisme de **mutual exclusion**.

## Observații privind ExecutorTests

Testele sunt împărțite în două categorii, vizând tipul de prioritate al task-urilor: _reader-priority_ & _writer-priority_.

Independent de tipul de prioritate, testele diferă prin parametrii asociați fiecărui scenariu:
- numărul de thread-uri din pool;
  - în funcție de numărul de core-uri disponibile pe sistem, se poate observa **scăderea timpului de execuție** odată cu creșterea numărului de thread-uri.
- numărul total de task-uri
  - afectează timpul de execuție al programului, punând în evidență atât mecanismele de sincronizare implementate, cât și distribuția uniformă a task-urilor.
- proporția de task-uri de tip Reader/Writer
  - e.g. în cazul testelor de tip reader-priority, task-urile de tip Reader sunt mai rare decât cele de tip Writer, pentru a verifica prioritizarea primelor.
  - pentru un număr mare de task-uri, se poate observa **scăderea timpului de execuție** odată cu creșterea proporției de task-uri din categoria prioritizată.
- numărul de intrări din baza de date & dimensiunea acestora
  - afectează timpul de execuție al operațiilor de citire/scriere.
- timpi de așteptare pentru verificarea sincronizării corecte
    - folosiți pentru a testa accesul exclusiv la intrările bazei de date.


