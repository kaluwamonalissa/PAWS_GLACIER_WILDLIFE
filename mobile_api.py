from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

app = FastAPI()

class FieldData(BaseModel):
    gps_coordinates: str
    wildlife_sightings: str
    threat_levels: str

@app.post("/upload_data/")
async def upload_data(data: FieldData):
    # Here you would implement the logic to handle the uploaded data.
    # For now, we'll simulate a successful upload.
    return {
        "message": "Data uploaded successfully",
        "data": data
    }
