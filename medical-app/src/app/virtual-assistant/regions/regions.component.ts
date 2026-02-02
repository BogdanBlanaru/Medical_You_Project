import { Component, OnInit } from '@angular/core';

declare const google: any;

@Component({
  selector: 'app-regions',
  templateUrl: './regions.component.html',
  styleUrls: ['./regions.component.css'],
})
export class RegionsComponent implements OnInit {
  map: any;
  searchAddress: string = '';
  nearbyFacilities: { name: string; address: string }[] = [];
  marker: any;

  ngOnInit(): void {
    // Initialize the map with a default location
    this.map = new google.maps.Map(document.getElementById('map'), {
      center: { lat: 37.7749, lng: -122.4194 }, // Default to San Francisco
      zoom: 12,
    });

    // Display current location on map
    this.displayCurrentLocation();

    // Add click listener to the map
    this.map.addListener('click', (event: any) => {
      const clickedLocation = event.latLng;
      this.placeMarker(clickedLocation);
      this.findNearbyFacilities(clickedLocation.lat(), clickedLocation.lng());
    });
  }

  displayCurrentLocation(): void {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const userLocation = {
            lat: position.coords.latitude,
            lng: position.coords.longitude,
          };
          this.map.setCenter(userLocation);
          this.placeMarker(userLocation);
          this.findNearbyFacilities(userLocation.lat, userLocation.lng);
        },
        (error) => {
          console.error('Error fetching geolocation: ', error);
        }
      );
    } else {
      console.error('Geolocation is not supported by this browser.');
    }
  }

  onSearchAddress(): void {
    if (!this.searchAddress.trim()) {
      alert('Please enter a valid address.');
      return;
    }

    const geocoder = new google.maps.Geocoder();
    geocoder.geocode({ address: this.searchAddress.trim() }, (results: any, status: any) => {
      if (status === 'OK') {
        const location = results[0].geometry.location;
        this.map.setCenter(location);
        this.placeMarker(location);
        this.findNearbyFacilities(location.lat(), location.lng());
      } else {
        console.error('Geocode was not successful for the following reason: ' + status);
      }
    });
  }

  placeMarker(location: any): void {
    if (this.marker) {
      this.marker.setMap(null);
    }
    this.marker = new google.maps.Marker({
      position: location,
      map: this.map,
    });
  }

  findNearbyFacilities(lat: number, lng: number): void {
    // Mock data for nearby facilities
    this.nearbyFacilities = [
      { name: 'General Hospital', address: '123 Health St, Nearby City' },
      { name: 'Family Clinic', address: '456 Wellness Blvd, Nearby Town' },
    ];
    console.log('Nearby facilities found:', this.nearbyFacilities);
  }

  goBack(): void {
    // Implement back navigation logic
  }

  proceed(): void {
    // Implement proceed logic
  }
}
