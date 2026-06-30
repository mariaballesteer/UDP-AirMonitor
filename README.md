# UDP AirMonitor

Sistema distribuido desarrollado en Java basado en comunicación mediante UDP para el intercambio de información entre clientes y servidores.

> Práctica de la asignatura **Programación Para las Comunicaciones (PPC)**.

---

## Autora

**María Ballester Martínez**

**Fecha de creación:** Junio 2026

---

## Descripción

Este proyecto implementa un sistema de monitorización basado en el protocolo UDP donde varios clientes pueden comunicarse con un servidor para intercambiar información.

El objetivo principal de la práctica es trabajar con:

- Comunicación mediante sockets UDP.
- Programación concurrente.
- Gestión de mensajes.
- Sincronización entre procesos.
- Serialización de información.
- Registro de eventos mediante ficheros JSON y XML.

El proyecto ha sido desarrollado utilizando Java y Maven.

---

## Tecnologías utilizadas

- Java 8
- Maven
- Eclipse
- UDP Sockets
- JSON
- XML

---

## Estructura del proyecto

```
Practica2-PPC/
│
├── src/
│   ├── main/
│   └── test/
│
├── logs/
│   ├── cliente_cli1/
│   ├── cliente_cli2/
│   └── servidor_srv1/
│
├── doc/
│   ├── Practica2_PPC.pdf
│   └── README.md
│
├── pom.xml
└── README.md
```

---

## Cómo ejecutar el proyecto

### Requisitos

- Java JDK 8 o superior.
- Maven instalado.
- Un IDE compatible con Maven (Eclipse, IntelliJ IDEA o VS Code).

### Clonar el repositorio

```bash
git clone https://github.com/TU_USUARIO/udp-airmonitor.git
```

Acceder al proyecto:

```bash
cd udp-airmonitor
```

Compilar:

```bash
mvn clean install
```

Ejecutar la aplicación desde el IDE o utilizando las clases principales del proyecto.

---

## Documentación

En la carpeta `doc/` se encuentra la memoria de la práctica donde se describe el diseño, la arquitectura y el funcionamiento del sistema.

---

## Registros

Durante la ejecución se generan archivos de registro en la carpeta `logs/`, tanto en formato JSON como XML, que almacenan el historial de comunicaciones entre clientes y servidor.

---

## Objetivos de aprendizaje

Este proyecto permitió poner en práctica conceptos relacionados con:

- Programación concurrente.
- Comunicación mediante UDP.
- Gestión de hilos.
- Arquitectura cliente-servidor.
- Serialización de datos.
- Diseño de aplicaciones distribuidas.
