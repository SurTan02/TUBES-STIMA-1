# TUBES-STIMA-1

> Merancang bot mobil dengan mengimplementasikan algoritma Greedy pada program sedemikian sehingga bot mobil dapat memenangi balapan menggunakan langkah paling optimum.

## Daftar Anggota Kelompok

<table>

<tr><td colspan = 3 align = "center">KELOMPOK 07 HADEH</td></tr>
<tr><td>No.</td><td>Nama</td><td>NIM</td></tr>
<tr><td>1.</td><td>Suryanto</td><td>13520059</td></tr>
<tr><td>2.</td><td>Vieri Mansyl</td><td>13520092</td></tr>
<tr><td>3.</td><td>Brianaldo Phandiarta</td><td>13520113</td></tr>

</table>

## Algoritma Greedy pada Program
Strategi yang diimplementasikan pada program ini merupakan gabungan dari greedy by speed dan greedy by point. Dalam pengimplementasiannya, akan diurutkan terlebih dahulu kandidat-kandidat yang memenuhi syarat berserta prioritas. Program akan memprediksi outcome dari setiap command pada suatu round. Selanjutnya program akan mengurutkan command berdasarkan speed yang diprediksi dan jika ditemukan lebih dari satu command maka program akan mengurutkan berdasarkan suatu atribut poin yang didefinisikan oleh kelompok kami. Command terpilih akan menjadi solusi dari permainan dan nantinya dieksekusi oleh bot sebagai command yang dianggap paling efisien di suatu round. 

## Requirement
* Overdrive 2020 - https://github.com/EntelectChallenge/2020-Overdrive/releases/tag/2020.3.4
* Java ( Minimal 8) - https://www.oracle.com/java/technologies/downloads/#java8
* NodeJS - https://nodejs.org/en/download/
* Intellij IDEA (Optional) - https://www.jetbrains.com/idea/
* Visualizer (Optional) - https://entelect-replay.raezor.co.za/

## Cara Menggunakan Program
1. Download starter-pack.zip dari https://github.com/EntelectChallenge/2020-Overdrive/releases/tag/2020.3.4
2. Ekstrak starter-pack.zip ke suatu folder
3. Download HADEH.jar pada folder bin dan masukkan ke sebuah folder dengan 
4. Ubah isi file game-runner-config.json pada bagian player-a atau di player-b dengan directory tempat file .jar dan bot.json dari repository berada. File .jar dan bot.json ada di folder bin di dalam repository ini.
5. Ubah bagian player lainnya di game-runner-config.json dengan directory bot lawan.
6. Jalankan file run.bat
7. Untuk menggunakan visualizer, download dan ekstrak zip visualizer dari https://github.com/dlweatherhead/entelect-challenge-2019-visualiser/releases/tag/v1.0f1, lalu ubah round-state-output-location di game-runner-config.json dengan directory folder Matches dalam folder visualizer yang sudah di ekstrak. Setelah menjalankan run.bat, jalankan entelect-visualiser.exe lalu pilih match yang diinginkan untuk ditampilkan.

## Directories
    .
    ├── doc                             # Documentation files (Laporan tugas besar)
    ├── src                             # Source files
    │    ├── frontend                   # Front End (source code untuk tampilan website menggunakan React.JS)
    │    └── backend/imageProcessing    # Back End (source code untuk kompresi gambar menggunakan python)
    ├── test                            # Images for testing
    └── README.md




