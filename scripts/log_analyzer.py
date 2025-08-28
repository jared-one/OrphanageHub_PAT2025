#!/usr/bin/env python3
import re
import argparse
from collections import Counter
from rich.console import Console
from rich.table import Table

LOG_PATTERN = re.compile(
    r"(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3})\s+"
    r"```math
(.*?)```\s+"
    r"(ERROR|WARN)\s+"
    r"([\w\.]+)\s+-\s+"
    r"(.*)"
)

def analyze_log(file_path):
    console = Console()
    errors = []
    error_sources = Counter()

    try:
        with open(file_path, 'r') as f:
            for line in f:
                match = LOG_PATTERN.match(line)
                if match:
                    timestamp, thread, level, logger, message = match.groups()
                    if level == "ERROR":
                        errors.append((timestamp, logger, message))
                        error_sources[logger] += 1
    except FileNotFoundError:
        console.print(f"[bold red]Log file not found: {file_path}[/bold red]")
        return

    console.print(f"\n[bold blue]Log Analysis Report for: {file_path}[/bold blue]")
    if not errors:
        console.print("[bold green]✅ No errors found.[/bold green]")
        return

    console.print(f"\n[bold red]Found {len(errors)} error(s).[/bold red]")
    console.print("\n[bold]Top Error Sources:[/bold]")
    for source, count in error_sources.most_common(5):
        console.print(f"- {count} from [cyan]{source}[/cyan]")

    table = Table(title="Error Details")
    table.add_column("Timestamp", style="dim")
    table.add_column("Source Class")
    table.add_column("Message")

    for ts, logger, msg in errors[:10]: # Display first 10
        table.add_row(ts, logger.split('.')[-1], msg)

    console.print(table)

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('file', nargs='?', default='logs/app.log')
    args = parser.parse_args()
    analyze_log(args.file)
