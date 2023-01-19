job "movie-service-job" {
  type = "service"
  datacenters = ["dc1"]

  update {
    max_parallel     = 1
    min_healthy_time = "30s"
    healthy_deadline = "2m"
  }

  group "movie-service-group" {
    count = 3

    network {
      port "grpc" {
        to = 50051
      }
    }

    service {
      name = "movie-service"
      port = "grpc"
      check {
        type     = "grpc"
        interval = "15s"
        timeout  = "5s"
      }
    }

    task "server" {
      driver = "docker"

      config {
        image = "ghcr.io/simonnordberg/cosmos-movie-service:main"
        ports = ["grpc"]
      }

      resources {
        cpu    = 140
        memory = 64
      }
    }
  }
}