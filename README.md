# Campus Navigation System

A mobile-based indoor and semi-indoor navigation system designed to help users efficiently navigate large institutional environments such as campuses, academic buildings, and administrative complexes.

---

## 🚀 Features

- Interactive map-based navigation
- Shortest path route computation
- Search and locate campus facilities
- Admin module for managing locations
- Cloud-based spatial data storage
- Dynamic route and location updates
- Distance-based path optimization

---

## 🛠️ Technologies Used

- Java
- Android Studio
- Firebase Firestore
- Google Maps API
- XML Layouts
- Graph Data Structures
- Shortest Path Algorithms

---

## 🧠 System Concept

The system represents the environment as a weighted graph:
- Nodes represent locations
- Edges represent walkable paths
- Weights represent traversal distance

The application computes optimized routes between selected locations and visualizes them on a map interface.

---

## 📂 Modules

### User Module
- Search locations
- Select destination
- View computed navigation path

### Admin Module
- Add/update locations
- Manage path connections
- Maintain navigation data

---

## ⚙️ Setup Instructions

### Clone Repository

```bash
git clone https://github.com/your-username/Campus-Navigation-System.git
```

### Configure API Key

Add your Google Maps API key inside:

```properties
local.properties
```

Example:

```properties
MAPS_API_KEY=YOUR_API_KEY
```

### Firebase Setup

Add your own:

```text
google-services.json
```

inside the `app/` directory.

---

## 🔐 Security

- Firebase rules are secured
- Sensitive API keys are excluded from the repository
- Maps API key is locally configured

---

## 📈 Future Scope

- Floor-wise indoor navigation
- Voice-assisted guidance
- Real-time indoor positioning
- Accessibility-aware routing

---

## 👨‍💻 Purpose

This project was developed for academic learning, research exploration, and practical implementation of indoor navigation concepts using graph-based routing techniques.
