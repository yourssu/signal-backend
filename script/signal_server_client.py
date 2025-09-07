import os
import json
import requests
from typing import Dict, Any, Optional


class ServerClient:
    def __init__(self):
        server_port = os.getenv('SERVER_PORT', '9011')
        self.base_url = f"http://localhost:{server_port}"
        self.secret_key = os.getenv('ADMIN_SECRET_KEY', '')
        
        if not self.secret_key:
            raise ValueError("ADMIN_SECRET_KEY environment variable is required")
    
    def add_to_blacklist(self, profile_id: str) -> Dict[str, Any]:
        try:
            url = f"{self.base_url}/api/blacklists"
            
            headers = {
                'Content-Type': 'application/json'
            }
            
            payload = {
                'profileId': int(profile_id),
                'secretKey': self.secret_key
            }
            
            response = requests.post(
                url,
                headers=headers,
                json=payload,
                timeout=10
            )
            
            if response.status_code == 201:
                print(f"Successfully added profile {profile_id} to blacklist")
                return {
                    'success': True,
                    'message': f"Profile {profile_id} added to blacklist",
                    'data': response.json()
                }
            else:
                error_message = f"Failed to add profile {profile_id} to blacklist: {response.status_code} - {response.text}"
                print(error_message)
                return {
                    'success': False,
                    'message': error_message,
                    'status_code': response.status_code
                }
                
        except requests.exceptions.Timeout:
            error_message = f"Timeout while adding profile {profile_id} to blacklist"
            print(error_message)
            return {
                'success': False,
                'message': error_message
            }
        except requests.exceptions.RequestException as e:
            error_message = f"Request error while adding profile {profile_id} to blacklist: {str(e)}"
            print(error_message)
            return {
                'success': False,
                'message': error_message
            }
        except Exception as e:
            error_message = f"Unexpected error while adding profile {profile_id} to blacklist: {str(e)}"
            print(error_message)
            return {
                'success': False,
                'message': error_message
            }
    
    def health_check(self) -> bool:
        try:
            url = f"{self.base_url}/actuator/health"
            response = requests.get(url, timeout=5)
            return response.status_code == 200
        except:
            return False


server_client = ServerClient()
