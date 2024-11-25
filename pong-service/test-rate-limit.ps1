$url = "http://localhost:8081/pong"
$results = @()

Write-Host "Starting rate limit test..."
Write-Host "Sending 5 requests in quick succession..."

for ($i = 1; $i -le 5; $i++) {
    $response = try {
        $result = Invoke-WebRequest -Uri $url -Method GET
        @{
            StatusCode = $result.StatusCode
            Content = $result.Content
        }
    } catch {
        @{
            StatusCode = $_.Exception.Response.StatusCode.value__
            Content = $_.Exception.Response.StatusDescription
        }
    }
    
    $results += @{
        RequestNumber = $i
        StatusCode = $response.StatusCode
        Content = $response.Content
    }
    
    Write-Host "Request $i - Status Code: $($response.StatusCode)"
}

Write-Host "`nTest Results:"
Write-Host "-------------"
foreach ($result in $results) {
    Write-Host "Request $($result.RequestNumber):"
    Write-Host "  Status Code: $($result.StatusCode)"
    Write-Host "  Content: $($result.Content)"
    Write-Host ""
}
